/*
 * Author: Tom Giallanza
 * Course: CSC 460 Database Design
 * Assignment: Program 2
 * Instructor: Dr. McCann
 * TAs: Jianwei Shen and Muhammad Bilal
 * Due Date: 02-12-2026
 *
 * Description:
 * This file defines the BinWriter class, which is responsible for writing
 * bat cave records to a fixed-width binary file compatible with the
 * BinReader class. Each Record object is written sequentially in a
 * fixed-width format determined by RecordLengths. A footer containing the
 * maximum string lengths for each variable-width field is appended to
 * the file to enable correct parsing by BinReader.
 *
 * The BinWriter class performs the following responsibilities:
 * 1. Opens a binary file for writing using RandomAccessFile.
 * 2. Writes all records to the binary file in fixed-width format.
 * 3. Writes footer metadata for RecordLengths.
 * 4. Closes the binary file stream safely.
 *
 * Operational Requirements:
 *   Language: Java 25.0.1
 *   Compilation: javac BinWriter.java
 *   Usage: Used by Prog1B to create binary files from Record objects
 *   Input: Binary file OS path and an ArrayList of Record objects
 *   Output: Binary file containing fixed-width records and footer metadata
 */

import java.io.*;
import java.util.*;

/*
 * Class: BinWriter
 * Author: Tom Giallanza
 * Purpose: An object of this class is responsible for writing immutable
 *          Record objects to a fixed-width binary file. Each record is
 *          written sequentially using maximum field lengths stored in
 *          RecordLengths, and a footer containing those lengths is
 *          appended to support correct parsing by BinReader.
 * Inherits From: None
 * Interfaces: Closeable
 * Constants: None
 * Constructors: BinWriter(File binFilePath)
 * Class Methods: None
 * Inst. Methods: void writeRecords(ArrayList<Record> records)
 *                void writeLengths()
 *                void close()
 */
class BinWriter {

    private File binFilePath;               // Binary OS file path
    private RandomAccessFile binFileStream; // Binary file stream

    /*
     * Constructor BinWriter(binFilePath)
     *
     * Purpose: Initializes a BinWriter object with a reference to the
     *          specified binary file path.
     * Pre-condition: binFilePath references a valid filesystem location.
     * Post-condition: The binary file path is stored for later use.
     * Parameters: binFilePath - Reference to the binary output file.
     */
    public BinWriter(File binFilePath) throws IOException {
        this.binFilePath = binFilePath; // Store binary file reference
    }

    /*
     * Method writeRecords(records)
     *
     * Purpose: Writes all Record objects to the binary file using a
     *          fixed-width layout determined by RecordLengths.
     *          Any existing file data is cleared before writing.
     * Pre-condition: RecordLengths values are initialized and the
     *                records list contains valid Record objects.
     * Post-condition: All records are written sequentially to the file.
     * Parameters: records - ArrayList of Record objects to write.
     */
    public void writeRecords(ArrayList<Record> records) throws IOException {
        // Remove existing file to clear old data
        if (binFilePath.exists())
            binFilePath.delete();

        // Open binary file stream for writing
        binFileStream = new RandomAccessFile(binFilePath, "rw");

        // Write each record sequentially
        for (Record record : records) {

            // Write all fixed-length string fields with proper padding
            binFileStream.writeBytes(
                Record.padField(record.getSeqID(), RecordLengths.maxSeqIDLen) +
                Record.padField(record.getEntry(), RecordLengths.maxEntryLen) +
                Record.padField(record.getSeries(), RecordLengths.maxSeriesLen) +
                Record.padField(record.getRealm(), RecordLengths.maxRealmLen) +
                Record.padField(record.getContinent(), RecordLengths.maxContinentLen) +
                Record.padField(record.getBiome(), RecordLengths.maxBiomeLen) +
                Record.padField(record.getCountry(), RecordLengths.maxCountryLen) +
                Record.padField(record.getCave(), RecordLengths.maxCaveLen)
            );

            // Write numeric coordinate fields
            binFileStream.writeDouble(record.getLatitude());
            binFileStream.writeDouble(record.getLongitude());

            // Write final fixed-length string field
            binFileStream.writeBytes(
                Record.padField(record.getSpecies(), RecordLengths.maxSpeciesLen)
            );
        }
    }

    /*
     * Method writeLengths()
     *
     * Purpose: Appends footer metadata containing the maximum string
     *          lengths for each variable-width field. This information
     *          is required by BinReader for correct record parsing.
     * Pre-condition: All records have been written to the file.
     * Post-condition: Footer metadata is appended to the binary file.
     */
    public void writeLengths() throws IOException {
        // Write maximum length of each string field
        binFileStream.writeInt(RecordLengths.maxSeqIDLen);
        binFileStream.writeInt(RecordLengths.maxEntryLen);
        binFileStream.writeInt(RecordLengths.maxSeriesLen);
        binFileStream.writeInt(RecordLengths.maxRealmLen);
        binFileStream.writeInt(RecordLengths.maxContinentLen);
        binFileStream.writeInt(RecordLengths.maxBiomeLen);
        binFileStream.writeInt(RecordLengths.maxCountryLen);
        binFileStream.writeInt(RecordLengths.maxCaveLen);
        binFileStream.writeInt(RecordLengths.maxSpeciesLen);
    }

    /*
     * Method close()
     *
     * Purpose: Closes the binary file stream and releases system resources.
     * Pre-condition: The binary file stream is open.
     * Post-condition: The file stream is closed.
     */
    public void close() throws IOException {
        if (binFileStream != null) binFileStream.close();
    }
}
