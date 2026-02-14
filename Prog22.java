/*
 * Author: Tom Giallanza
 * Course: CSC 460 Database Design
 * Assignment: Program 2
 * Instructor: Dr. McCann
 * TAs: Jianwei Shen and Muhammad Bilal
 * Due Date: 02-12-2026
 *
 * Description:
 * This file defines the Prog22 class, which provides an interactive
 * search interface for a fixed-width binary dataset using a
 * pre-generated extendible hash index. It allows users to retrieve
 * records by Data.entry value without performing a full sequential
 * scan of the binary file.
 *
 * The Prog22 program performs the following responsibilities:
 * 1. Determines index and binary file paths from command-line input.
 * 2. Initializes an IndexReader for indexed access.
 * 3. Loads the hash iteration value from the index file.
 * 4. Prompts the user for Data.entry search keys.
 * 5. Retrieves and displays matching records using the index.
 * 6. Terminates execution when a sentinel value is entered.
 *
 * Operational Requirements:
 *   Language: Java 25.0.1
 *   Compilation: javac Prog22.java
 *   Usage: java Prog22 <index file> <binary file>
 *   Input: Index file and fixed-width binary data file
 *   Output: Matching records printed to standard output
 */

import java.io.*;
import java.util.*;

class Prog22 {

    /*
     * Method getIndexFilePath(args)
     *
     * Purpose: Determines the file system path of the index file.
     *          If command-line arguments are provided, the first
     *          argument is treated as the index file path.
     * Pre-condition: args is not null.
     * Post-condition: A File object referencing the index file is returned.
     * Parameters: args - Command-line arguments passed to the program.
     * Returns: A File object representing the index file path.
     */
    public static File getIndexFilePath(String[] args) throws IOException {
        File indexFilePath;   // Stores the index file path

        // Use command-line argument if provided
        if (args.length == 2)
            indexFilePath = new File(args[0]);
        else
            indexFilePath = new File(Consts.INDEX_FILE_NAME);

        return indexFilePath;
    }

    /*
     * Method getBinaryFilePath(args)
     *
     * Purpose: Determines the file system path of the binary data file.
     *          If command-line arguments are provided, the second
     *          argument is treated as the binary file path.
     * Pre-condition: args is not null.
     * Post-condition: A File object referencing the binary file is returned.
     * Parameters: args - Command-line arguments passed to the program.
     * Returns: A File object representing the binary file path.
     */
    public static File getBinaryFilePath(String[] args) throws IOException {
        File binFilePath;   // Stores the binary file path

        // Use command-line argument if provided
        if (args.length == 2)
            binFilePath = new File(args[1]);
        else
            binFilePath = new File(Consts.BIN_FILE_NAME);

        return binFilePath;
    }

    /*
     * Method searchIndex(indexFilePath, binFilePath)
     *
     * Purpose: Performs interactive indexed searches on the binary
     *          dataset by prompting the user for Data.entry values
     *          and retrieving matching records using the index.
     * Pre-condition: Index and binary files exist and are readable.
     * Post-condition: Matching records are printed to standard output.
     * Parameters: indexFilePath - Reference to the index file.
     *             binFilePath   - Reference to the binary data file.
     */
    public static void searchIndex(File indexFilePath, File binFilePath) throws IOException {
        IndexReader indexReader;    // Used to read and query the index
        indexReader = new IndexReader(indexFilePath, binFilePath);

        Scanner scanner = new Scanner(System.in);   // Reads console input
        Record fetchRes;    // Holds lookup result

        // Repeatedly prompt the user for search keys
        while (true) {
            System.out.print("Enter a Data.entry value or -1000 to stop searching: ");

            String input = scanner.nextLine(); // User-entered search key

            // Stop searching on sentinel value
            if (input.equals("-1000"))
                break;

            // Attempt to fetch the record using the index
            fetchRes = indexReader.fetchRecord(input);

            // Print the result of the fetch
            if (fetchRes == null)
                System.out.println("The target value " + input + " was not found.");
            else
                System.out.println(fetchRes);
        }

        // Close the file stream and the scanner when we're done
        indexReader.close();
        scanner.close();
    }

    public static void main(String[] args) {
        try {
            // Process the arguments
            if (args.length != 0 && args.length != 2)
                throw new IllegalArgumentException("Usage: <program> [<indexFilePath> <binaryFilePath>]");

            File indexFilePath = getIndexFilePath(args);
            File binFilePath   = getBinaryFilePath(args);

            // Search the index
            searchIndex(indexFilePath, binFilePath);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}
