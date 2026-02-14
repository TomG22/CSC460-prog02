/*
 * Author: Tom Giallanza
 * Course: CSC 460 Database Design
 * Assignment: Program 2
 * Instructor: Dr. McCann
 * TAs: Jianwei Shen and Muhammad Bilal
 * Due Date: 02-12-2026
 *
 * Description:
 * This file defines the Consts class, which stores global constant values
 * used throughout the program. These constants include file names and
 * fixed parameters related to the extendible hash index structure.
 *
 * The Consts class performs the following responsibilities:
 * 1. Defines standard file names for CSV, binary, and index files.
 * 2. Defines bucket capacity for the extendible hash index.
 * 3. Defines the computed byte size of each hash bucket.
 *
 * Operational Requirements:
 *   Language: Java 25.0.1
 *   Compilation: javac Consts.java
 *   Usage: Referenced by IndexWriter, IndexReader, and other program files
 *   Input: None
 *   Output: Provides shared constant values
 */

/*
 * Class: Consts
 * Author: Tom Giallanza
 * Purpose: This class stores program-wide constant values used for
 *          file naming and index configuration. It centralizes
 *          configuration parameters to ensure consistency across
 *          all components of the system.
 * Inherits From: None
 * Interfaces: None
 * Constants: INDEX_FILE_NAME
 *            BIN_FILE_NAME
 *            CSV_FILE_NAME
 *            BUCKET_CAPACITY
 *            BUCKET_SIZE_BYTES
 * Constructors: None
 * Class Methods: None
 * Inst. Methods: None
 */
class Consts {

    // File Name Constants
    public static final String INDEX_FILE_NAME = "lhl.idx"; 
        // Name of extendible hash index file

    public static final String BIN_FILE_NAME = "Dataset2.bin"; 
        // Name of fixed-width binary data file

    public static final String CSV_FILE_NAME = "Dataset2.csv"; 
        // Name of original CSV dataset file


    // Index Structure Constants
    public static final int BUCKET_CAPACITY = 30; 
        // Maximum number of record pointers stored per bucket

    public static final int BUCKET_SIZE_BYTES =
        (Integer.BYTES + Long.BYTES * BUCKET_CAPACITY);
        // Total byte size of a bucket:
        // 4 bytes for bucket size (int)
        // + 8 bytes per record pointer (long)

}

