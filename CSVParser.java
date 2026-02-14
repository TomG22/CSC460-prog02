/*
 * Author: Tom Giallanza
 * Course: CSC 460 Database Design
 * Assignment: Program 2
 * Instructor: Dr. McCann
 * TAs: Jianwei Shen and Muhammad Bilal
 * Due Date: 02-12-2026
 *
 * Description:
 * This file defines the CSVParser class, which is responsible for reading
 * biological dataset records from a CSV file and converting them into
 * immutable Record objects. It also tracks the maximum lengths of all 
 * variable-width fields in the dataset to support fixed-width binary 
 * output compatible with BinWriter and BinReader.
 *
 * The CSVParser class performs the following responsibilities:
 * 1. Opens and reads a CSV file using a BufferedReader.
 * 2. Parses individual CSV fields while handling quoted strings and commas.
 * 3. Converts rows into immutable Record objects.
 * 4. Updates RecordLengths to track maximum string sizes.
 * 5. Provides a method to read the entire CSV into an ArrayList of Records.
 *
 * Operational Requirements:
 *   Language: Java 25.0.1
 *   Compilation: javac CSVParser.java
 *   Usage: Used by Prog1A or BinWriter to create binary files from CSV input
 *   Input: CSV file of bat cave records
 *   Output: ArrayList of immutable Record objects and updated RecordLengths
 */

import java.io.*;
import java.util.*;

/*
 * Class: CSVParser
 * Author: Tom Giallanza
 * Purpose: An object of this class is responsible for parsing a CSV file
 *          containing biological dataset records. It reads individual
 *          fields, constructs immutable Record objects, tracks maximum
 *          string field lengths, and writes the data to a fixed-length
 *          binary file suitable for random access.
 * Inherits From: None
 * Interfaces: None
 * Constants: None
 * Constructors: CSVParser(File file)
 * Class Methods: None
 * Inst. Methods: String parseString()
 *                Record parseRecord()
 *                void parseCSV()
 */
class CSVParser {
    private BufferedReader csvFileStream;   // Input CSV file stream

    /*
     * Constructor CSVParser(file)
     *
     * Purpose: Initializes a CSVParser object with a BufferedReader
     *          connected to the specified CSV file.
     * Pre-condition: File parameter must reference a valid, readable CSV file
     * Post-condition: A BufferedReader is created and initialized
     * Parameters: file - Reference to the CSV file to be parsed.
     */
    CSVParser(File file) throws IOException {
        csvFileStream = new BufferedReader(new FileReader(file));
    }

    /*
     * Method parseString()
     *
     * Purpose: Reads and returns the next field from the CSV input.
     *          Fields may be enclosed in quotes and may contain commas.
     *          Parsing stops at an unquoted comma, newline, or EOF.
     * Pre-condition: The BufferedReader is open and positioned at the
     *                start of a CSV field.
     * Post-condition: The file pointer is positioned immediately after
     *                 the field delimiter.
     * Returns: A String containing the parsed field, or null if EOF
     *           is reached before any characters are read.
     */
    public String parseString() throws IOException {
        StringBuilder str = new StringBuilder(); // Accumulates characters of the current field

        boolean inQuote = false; // Tracks whether we are inside a quoted string
        int readResult; // Holds the result of csvFileStream.read() calls

        // Append to the string result until we hit EOF
        while ((readResult = csvFileStream.read()) != -1) {
            char c = (char) readResult; // Current character being processed

            // We reached the end of a field marked by a comma or newline, return it
            if ((c == ',' || c == '\n') && !inQuote)
                return str.toString().trim();

            // If we hit a quote, skip over it and update the quote state
            // Otherwise, consume a normal character
            if (c == '"')
                inQuote = !inQuote;
            else
                str.append(c);
        }

        // We unexpectedly hit EOF inside of a row
        if (str.length() != 0)
            throw new IOException("Error: Unterminated quote in CSV field");

        // We hit EOF without parsing any fields
        return null;
    }


    /*
     * Method parseRecord()
     *
     * Purpose: Reads and parses one complete CSV record and converts
     *          it into an immutable Record object. While parsing,
     *          maximum field lengths are updated for binary formatting.
     * Pre-condition: The CSV file stream is positioned at the start of a record.
     * Post-condition: The CSV file stream is positioned at the start of the next
     *                 record (or EOF).
     * Returns: A populated Record object, or null if EOF is reached.
     */
    public Record parseRecord() throws IOException {
        String seqID     = parseString();   // Dataset sequence ID field

        // Check if we hit EOF on this row
        if (seqID == null) return null;

        // Parse all of the expected fields
        String entry     = parseString();   // Data.entry field
        String series    = parseString();   // Data.series field
        String realm     = parseString();   // Biogeographicalal.realm field
        String continent = parseString();   // Continent field
        String biome     = parseString();   // Biome field
        String country   = parseString();   // Country.record field
        String cave      = parseString();   // Cave.site field
        String latitude  = parseString();   // Latitude field (as string, will be converted)
        String longitude = parseString();   // Longitude field (as string, will be converted)
        String species   = parseString();   // Species.name field

        // If any of the fields weren't parsed correctly, throw an error
        if (entry == null ||
            series == null ||
            realm == null ||
            continent == null ||
            biome == null ||
            country == null ||
            cave == null ||
            latitude == null ||
            longitude == null ||
            species == null) {
            throw new IOException("Error: A CSV field is incorrectly formatted");
        }

        // If either coordinate field is missing, fill it with a placeholder
        if (longitude == "") longitude = "-1000";
        if (latitude == "") latitude = "-1000";

        // Create a new immutable data record
        Record record = new Record(
            seqID,
            entry,
            series,
            realm,
            continent,
            biome,
            country,
            cave,
            Double.parseDouble(latitude),
            Double.parseDouble(longitude),
            species
        );

        // If any of field is missing, make the length as wide as "null"
        seqID = Record.nullIfEmpty(seqID);
        entry = Record.nullIfEmpty(entry);
        series = Record.nullIfEmpty(series);
        realm = Record.nullIfEmpty(realm);
        continent = Record.nullIfEmpty(continent);
        biome = Record.nullIfEmpty(biome);
        country = Record.nullIfEmpty(country);
        cave = Record.nullIfEmpty(cave);
        species = Record.nullIfEmpty(species);

        // Update the maximum lengths of the parsed fields
        RecordLengths.maxSeqIDLen      = Math.max(RecordLengths.maxSeqIDLen,      seqID.length());
        RecordLengths.maxEntryLen      = Math.max(RecordLengths.maxEntryLen,      entry.length());
        RecordLengths.maxSeriesLen     = Math.max(RecordLengths.maxSeriesLen,     series.length());
        RecordLengths.maxRealmLen      = Math.max(RecordLengths.maxRealmLen,      realm.length());
        RecordLengths.maxContinentLen  = Math.max(RecordLengths.maxContinentLen,  continent.length());
        RecordLengths.maxBiomeLen      = Math.max(RecordLengths.maxBiomeLen,      biome.length());
        RecordLengths.maxCountryLen    = Math.max(RecordLengths.maxCountryLen,    country.length());
        RecordLengths.maxCaveLen       = Math.max(RecordLengths.maxCaveLen,       cave.length());
        RecordLengths.maxSpeciesLen    = Math.max(RecordLengths.maxSpeciesLen,    species.length());

        return record;
    }

    /*
     * Method parseCSV()
     *
     * Purpose: Reads the entire CSV file, converts each row into a
     *          Record object, and writes the content to a fixed-length
     *          binary file named "Dataset2.bin".
     * Pre-condition: The CSV file exists, is readable, and correctly
     *                formatted.
     * Post-condition: The binary file is created and populated with
     *                 uniformly-sized records.
     * Returns: An ArrayList of Record objects, one per row  
     */
    public ArrayList<Record> parseCSV() throws IOException {
        ArrayList<Record> records = new ArrayList<Record>();    // Array of records to be stored
        Record currRecord; // Holds the current record being processed

        // Consume the header
        csvFileStream.readLine(); 

        // Read the rest of the rows in the file
        while ((currRecord = parseRecord()) != null)
            records.add(currRecord);

        // Close the file stream when done reading
        csvFileStream.close();

        return records;
    }

}
