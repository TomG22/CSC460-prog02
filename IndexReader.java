/*
 * Author: Tom Giallanza
 * Course: CSC 460 Database Design
 * Assignment: Program 2
 * Instructor: Dr. McCann
 * TAs: Jianwei Shen and Muhammad Bilal
 * Due Date: 02-12-2026
 *
 * Description:
 * This file defines the IndexReader class, which is responsible for reading
 * and querying an extendible hash index associated with a fixed-width
 * binary dataset. The index enables fast record retrieval by Data.entry
 * values using stored file offsets rather than sequential scanning.
 *
 * The IndexReader class performs the following responsibilities:
 * 1. Reads the hash iteration value used during index construction.
 * 2. Computes bucket addresses using the extendible hash function.
 * 3. Reads bucket contents from the index file.
 * 4. Retrieves records from the binary file using stored offsets.
 * 5. Optionally prints the index contents for debugging and inspection.
 *
 * Operational Requirements:
 *   Language: Java 25.0.1
 *   Compilation: javac IndexReader.java
 *   Usage: Used by Prog22 to retrieve records via indexed lookup
 *   Input: Extendible hash index file and fixed-width binary data file
 *   Output: Record objects matching requested Data.entry values
 */

import java.io.*;
import java.util.*;

/*
 * Class: IndexReader
 * Author: Tom Giallanza
 * Purpose: An object of this class provides read-only access to an
 *          extendible hash index and its associated binary data file.
 *          It supports efficient retrieval of records by Data.entry
 *          value using stored file offsets.
 * Inherits From: None
 * Interfaces: None
 * Constants: None
 * Constructors: IndexReader(File binFilePath)
 * Class Methods: None
 * Inst. Methods: int getNumBuckets()
 *                int hash(String entryID)
 *                Record fetchRecord(String entryID)
 *                void cacheHashIter()
 *                void close()
 */
class IndexReader {

    private int hashIter = 0;   // Hash iteration value used to size hash table
    private BinReader binReader; // Utility for reading binary records

    private File indexFilePath;               // Index file path
    private RandomAccessFile indexFileStream; // Random-access index file stream

    /*
     * Constructor IndexReader(binFilePath)
     *
     * Purpose: Initializes an IndexReader object for the specified binary
     *          data file and opens the associated index file for random
     *          access reading operations.
     * Pre-condition: binFilePath references a valid binary data file and
     *                the index file exists and is readable.
     * Post-condition: The index file stream is opened and hashIter is loaded.
     * Parameters: binFilePath - Reference to the binary data file.
     */
    public IndexReader(File indexFilePath, File binFilePath) throws IOException {
        binReader = new BinReader(binFilePath); // Initialize binary reader
        indexFileStream = new RandomAccessFile(indexFilePath, "r");
        this.indexFilePath = indexFilePath;
            // Open the index file for reading
        cacheHashIter(); // Load hash iteration value from disk
    }

    /*
     * Method getNumBuckets()
     *
     * Purpose: Computes the total number of hash buckets based on
     *          the current hash iteration value.
     * Pre-condition: hashIter has been initialized.
     * Post-condition: No state is modified.
     * Returns: The total number of hash buckets.
     */
    public int getNumBuckets() {
        return (int) Math.pow(2, hashIter + 1); // Bucket count doubles per iteration
    }

    /*
     * Method hash(entryID)
     *
     * Purpose: Computes the hash bucket index corresponding to the
     *          given Data.entry value.
     * Pre-condition: entryID is non-null and hashIter is initialized.
     * Post-condition: No state is modified.
     * Parameters: entryID - Data.entry value to be hashed.
     * Returns: Integer bucket index.
     */
    public int hash(String entryID) {
        // Ensure non-negative hash before modulo operation
        return (entryID.hashCode() & 0x7fffffff) % getNumBuckets();
    }

    /*
     * Method fetchRecord(entryID)
     *
     * Purpose: Retrieves a record whose Data.entry matches the
     *          provided entryID using indexed lookup.
     * Pre-condition: The index file has been initialized and hashIter loaded.
     * Post-condition: Index and binary file streams remain open.
     * Parameters: entryID - Data.entry value to search for.
     * Returns: Matching Record object if found; null otherwise.
     */
    public Record fetchRecord(String entryID) throws IOException {
        if (indexFilePath.length() == 0) return null;

        // Compute bucket byte offset
        long bucketPtr =
            (long) hash(entryID.trim()) * Consts.BUCKET_SIZE_BYTES;

        indexFileStream.seek(bucketPtr);

        int bucketSize = indexFileStream.readInt(); // Read bucket occupancy

        long currRecordPtr;
        Record currRecord;

        // Search only occupied slots
        for (int i = 0; i < bucketSize; i++) {

            currRecordPtr = indexFileStream.readLong(); // Read record offset
            currRecord = binReader.readRecord(currRecordPtr); // Fetch record

            if (currRecord.getEntry().trim().equals(entryID))
                return currRecord; // Match found
        }

        return null; // No matching entry found
    }

    /*
     * Method cacheHashIter()
     *
     * Purpose: Reads the hash iteration value stored at the end of
     *          the index file and loads it into memory.
     * Pre-condition: The index file exists and is readable.
     * Post-condition: hashIter reflects the value used during index creation.
     */
    public void cacheHashIter() throws IOException {
        // If the index file has a hashIter at the end, read it into memory
        if (indexFilePath.length() != 0) {
            // Seek to final integer stored in index file
            indexFileStream.seek(indexFilePath.length() - Integer.BYTES);

            hashIter = indexFileStream.readInt(); // Load hash iteration value
        }
        // If the index file is empty, just assume 0 (no buckets)
        else {
            hashIter = 0;
        }
    }

    /*
     * Method close()
     *
     * Purpose: Closes the index file stream and the associated
     *          binary file reader, releasing all system resources.
     * Pre-condition: The IndexReader has been initialized.
     * Post-condition: All open file streams are closed.
     */
    public void close() throws IOException {
        if (indexFileStream != null) indexFileStream.close();
        if (binReader != null) binReader.close();
    }
}
