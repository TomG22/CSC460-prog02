/*
 * Author: Tom Giallanza
 * Course: CSC 460 Database Design
 * Assignment: Program 2
 * Instructor: Dr. McCann
 * TAs: Jianwei Shen and Muhammad Bilal
 * Due Date: 01-29-2026
 *
 * Description:
 * This program reads a CSV file containing global bat cave data and converts
 * it into a fixed-width binary file suitable for efficient random access and
 * searching.
 *
 * The program performs the following operations when executed:
 * 1. Reads and parses the CSV file, ignoring the header row and correctly
 *    handling quoted fields and embedded commas
 * 2. Extracts 11 fields per record and constructs Record objects
 * 3. Sorts all records in ascending order by the Data.entry field
 * 4. Determines the maximum string length required for each string field
 *    across all records
 * 5. Writes all records to a binary file using a fixed-width record format,
 *    padding string fields with spaces as necessary
 * 6. Appends the maximum string lengths to the end of the binary file as a
 *    footer for use by the companion program (Prog1B.java)
 *
 * The binary file uses a consistent record structure where each field occupies
 * a fixed number of bytes, making random access possible. String fields are
 * padded with spaces to reach their maximum length, and numeric fields use
 * standard Java binary representations.
 *
 * Operational Requirements:
 *   Language: Java 25.0.1
 *   Compilation: javac Prog1A.java
 *   Execution: java Prog1A <CSV File Path>
 *   Input: CSV file containing bat cave data (default: Dataset2.csv)
 *   Output: Fixed-width binary file with a .bin extension
 */

import java.io.*;
import java.util.*;

class Prog1A {

    /*
     * Determines the file system path of the input CSV file.
     *
     * If a command-line argument is provided, it is used as the CSV file
     * path. Otherwise, the program defaults to "Dataset2.csv". The method
     * validates that the file exists and is readable before returning it.
     *
     * @param args Command-line arguments
     * @return File object representing the CSV file path
     * @throws IOException if file validation fails
     */
    public static File getCSVFilePath(String[] args) throws IOException {
        File csvFilePath;   // Holds the CSV's OS file path

        // If an argument was provided, use it as the CSV file's path name
        if (args.length > 0)
            csvFilePath = new File(args[0]);
        // Otherwise, default to Dataset2.csv
        else
            csvFilePath = new File("Dataset2.csv");

        // Validate that the CSV file exists
        if (!csvFilePath.exists()) {
            System.out.println("Error: CSV file does not exist");
            System.exit(-1);
        }

        // Validate that the CSV file can be read
        if (!csvFilePath.canRead()) {
            System.out.println("Error: CSV file cannot be read");
            System.exit(-1);
        }

        return csvFilePath;
    }

    /*
     * Parses the CSV file and converts each row into a Record object.
     *
     * This method uses a CSVParser to read and parse the CSV file, then
     * sorts the resulting records in ascending order based on their
     * entry identifier field.
     *
     * @param csvFilePath File object referencing the CSV file
     * @return ArrayList of parsed and sorted Record objects
     * @throws IOException if CSV parsing fails
     */
    public static ArrayList<Record> parseCSV(File csvFilePath) throws IOException {
        CSVParser csvParser = new CSVParser(csvFilePath);   // CSVParser object for processing the CSV file

        // Parse the rows of the CSV and extract the records
        ArrayList<Record> records = csvParser.parseCSV();  // List of records from the CSV

        // Sort the records by their entry.ID field
        records.sort(Record.compareEntry);

        return records;
    }

    /*
     * Writes the parsed records to a fixed-width binary file.
     *
     * The output binary file shares the same base name as the input CSV
     * file, with a ".bin" extension. All records are written in sorted
     * order, followed by a footer containing maximum field lengths.
     *
     * @param csvFilePath File object referencing the input CSV file
     * @param records List of parsed and sorted Record objects
     * @throws IOException if binary file writing fails
     */
    public static void writeBinaryFile(File csvFilePath, ArrayList<Record> records) throws IOException {
        String filePrefix = csvFilePath
                            .getName()
                            .substring(0, csvFilePath.getName().lastIndexOf('.')); // Name of the file argument

        File binFilePath = new File(filePrefix + ".bin");   // Reference to the binary output file
        BinWriter binWriter = new BinWriter(binFilePath);   // Object for writing to the binary file

        // Write the records to the binary file
        binWriter.writeRecords(records);

        // Write the maximum field lengths as a footer
        binWriter.writeLengths();

        // Close the binary file stream when done
        binWriter.close();
    }

    /*
     * Program entry point.
     *
     * Coordinates CSV file validation, parsing, record sorting, and
     * binary file generation. Any I/O errors encountered during
     * execution result in program termination.
     *
     * @param args Command-line arguments
     */
    public static void main (String [] args) {
        try {
            // Get the CSV file Path
            File csvFilePath = getCSVFilePath(args);

            // Parse the CSV
            ArrayList<Record> records = parseCSV(csvFilePath);

            // Write the binary file
            writeBinaryFile(csvFilePath, records);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }        
    }

}
