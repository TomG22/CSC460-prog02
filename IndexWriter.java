/*
 * Author: Tom Giallanza
 * Course: CSC 460 Database Design
 * Assignment: Program 2
 * Instructor: Dr. McCann
 * TAs: Jianwei Shen and Muhammad Bilal
 * Due Date: 02-12-2026
 *
 * Description:
 * This file defines the IndexWriter class, which is responsible for
 * constructing and maintaining an extendible hash index for a fixed-width
 * binary dataset. The index dynamically grows as buckets become full and
 * stores record offsets that enable fast, indexed access to records by
 * Data.entry values.
 *
 * The IndexWriter class performs the following responsibilities:
 * 1. Initializes the index file and hash table structure.
 * 2. Inserts record pointers into hash buckets based on Data.entry values.
 * 3. Detects bucket overflow and expands the index accordingly.
 * 4. Rebuilds the index when the hash table grows.
 * 5. Writes the final hash iteration value to the index file.
 *
 * Operational Requirements:
 *   Language: Java 25.0.1
 *   Compilation: javac IndexWriter.java
 *   Usage: Used by Prog21 to build the extendible hash index
 *   Input: Fixed-width binary data file of dataset records
 *   Output: Extendible hash index file containing record offsets
 */

import java.io.*;
import java.util.*;

/*
 * Class: IndexWriter
 * Author: Tom Giallanza
 * Purpose: An object of this class is responsible for building and
 *          maintaining an extendible hash index over a fixed-width
 *          binary data file. It inserts record pointers into hash
 *          buckets, expands the index when buckets overflow, and
 *          writes metadata required for indexed lookup operations.
 * Inherits From: None
 * Interfaces: None
 * Constants: None
 * Constructors: IndexWriter(File binFilePath)
 * Class Methods: None
 * Inst. Methods: int getNumBuckets()
 *                int hash(String entryID)
 *                void initIndexRange(int start, int end)
 *                void clearSizeRange(int start, int end)
 *                boolean insertRecord(Record record, long recordPtr)
 *                void clearIndex()
 *                void populateIndex()
 *                void writeHashIter()
 *                void close()
 */
class IndexWriter {

    private int hashIter = 0;   // Current hash iteration value (controls table size)
    private BinReader binReader; // Utility for reading records from binary file

    private File indexFilePath;               // Index file path
    private RandomAccessFile indexFileStream; // Random-access index file stream

    /*
     * Constructor open(binFilePath)
     *
     * Purpose: Initializes an IndexWriter object with a RandomAccessFile
     *          for the extendible hash index and a BinReader for reading
     *          the fixed-width binary dataset.
     * Pre-condition: The binary file exists and is readable.
     * Post-condition: IndexWriter is ready to build or modify the index.
     * Parameters:
     *   binFilePath - Reference to the fixed-width binary data file
     */
    public void open(File binFilePath) throws IOException {
        binReader = new BinReader(binFilePath); // Initialize binary reader
        indexFilePath = new File(Consts.INDEX_FILE_NAME); // Create index file in the cwd
        indexFileStream = new RandomAccessFile(indexFilePath, "rw"); // Open for read/write
    }

    /*
     * Method getNumBuckets()
     *
     * Purpose: Computes the current number of hash buckets based on
     *          the current hash iteration.
     * Pre-condition: hashIter is correctly initialized.
     * Post-condition: No internal state is modified.
     * Returns: The number of hash buckets available at the current iteration.
     */
    public int getNumBuckets() {
        // Number of buckets doubles each time hashIter increases
        return (int) Math.pow(2, hashIter + 1);
    }

    /*
     * Method hash(entryID)
     *
     * Purpose: Computes the bucket index for a given entry ID using
     *          a hash function and modulo operation based on the
     *          current number of buckets.
     * Pre-condition: entryID is a non-null string.
     * Post-condition: No internal state is modified.
     * Parameters:
     *   entryID - The Data.entry string used for hashing
     * Returns: The computed bucket index for the entry.
     */
    public int hash(String entryID) {
        // Ensure non-negative hash value before modulo
        return (entryID.hashCode() & 0x7fffffff) % getNumBuckets();
    }

    /*
     * Method initIndexRange(start, end)
     *
     * Purpose: Initializes a range of buckets in the index file by
     *          setting their sizes to zero and filling record pointer
     *          slots with dummy values (-1).
     * Pre-condition: start and end define a valid bucket range.
     * Post-condition: Specified buckets are ready to accept record pointers.
     * Parameters:
     *   start - The starting bucket index (inclusive)
     *   end   - The ending bucket index (exclusive)
     */
    public void initIndexRange(int start, int end) throws IOException {
        for (int i = start; i < end; i++) {

            // Move file pointer to start of bucket
            indexFileStream.seek(Consts.BUCKET_SIZE_BYTES * i);

            indexFileStream.writeInt(0); // Initialize bucket size to 0

            // Fill bucket record pointer slots with dummy values
            for (int j = 0; j < Consts.BUCKET_CAPACITY; j++) {
                indexFileStream.writeLong(-1);
            }
        }
    }

    /*
     * Method clearSizeRange(start, end)
     *
     * Purpose: Resets the size field of each bucket in a specified range
     *          to zero without altering the bucket contents.
     * Pre-condition: start and end define a valid bucket range.
     * Post-condition: Bucket sizes are cleared; record pointers remain.
     * Parameters:
     *   start - The starting bucket index (inclusive)
     *   end   - The ending bucket index (exclusive)
     */
    public void clearSizeRange(int start, int end) throws IOException {
        for (int i = start; i < end; i++) {

            // Seek to bucket size field only
            indexFileStream.seek(Consts.BUCKET_SIZE_BYTES * i);

            // Reset the size of the bucket without clearing the body
            indexFileStream.writeInt(0);
        }
    }

    /*
     * Method insertRecord(record, recordPtr)
     *
     * Purpose: Inserts a record pointer into the appropriate hash bucket
     *          in the index file. If the bucket is full, insertion fails.
     * Pre-condition: The record object is valid, and recordPtr points
     *                to a valid location in the binary file.
     * Post-condition: The record pointer is written to the bucket, and
     *                 the bucket size is updated if insertion succeeds.
     * Parameters:
     *   record    - The Record object to insert
     *   recordPtr - Byte offset of the record in the binary file
     * Returns: True if the record was successfully inserted; false if
     *          the bucket was full.
     */
    public boolean insertRecord(Record record, long recordPtr) throws IOException {

        // Compute byte offset of target bucket
        long bucketPtr =
            (long) hash(record.getEntry().trim()) * Consts.BUCKET_SIZE_BYTES;

        indexFileStream.seek(bucketPtr);

        int bucketSize = indexFileStream.readInt(); // Read current bucket occupancy

        // If bucket is full, signal overflow
        if (bucketSize == Consts.BUCKET_CAPACITY)
            return false;

        // Move to next available slot in bucket
        indexFileStream.seek(bucketPtr + Integer.BYTES + bucketSize * Long.BYTES);

        indexFileStream.writeLong(recordPtr); // Write record pointer

        // Update bucket size
        indexFileStream.seek(bucketPtr);
        indexFileStream.writeInt(bucketSize + 1);

        return true;
    }

    /*
     * Method clearIndex()
     *
     * Purpose: Completely clears the index file by truncating its size.
     * Pre-condition: The index file exists and is writable.
     * Post-condition: Index file is empty and ready for re-initialization.
     */
    public void clearIndex() throws IOException {
        indexFilePath = new File(Consts.INDEX_FILE_NAME);
            // Assume the index file path is in the cwd
        if (indexFilePath.exists()) {
            indexFilePath.delete();
        }
    }

    /*
     * Method populateIndex()
     *
     * Purpose: Reads all records from the binary dataset and inserts
     *          their pointers into hash buckets. If a bucket overflows,
     *          the index grows and insertion restarts.
     * Pre-condition: Binary dataset and index file are initialized.
     * Post-condition: All records are inserted into the extendible hash index.
     */
    public void populateIndex() throws IOException {
        long currRecordPtr; // Byte offset of current record
        Record currRecord;  // Record read from binary file

        // Initialize initial bucket range
        initIndexRange(0, getNumBuckets());

        // Attempt to insert every record
        int i = 0;  // Record index
        while (i < binReader.getNumRecords()) {

            currRecordPtr = i * binReader.getSizeOfRecord(); // Compute record offset
            currRecord = binReader.readRecord(currRecordPtr); // Read record

            boolean writeRes = insertRecord(currRecord, currRecordPtr);

            // If insertion fails, expand hash table
            if (!writeRes) {
                // Reset existing bucket sizes
                clearSizeRange(0, getNumBuckets());

                // Initialize new bucket range (doubling table)
                initIndexRange(getNumBuckets(), getNumBuckets() * 2);

                hashIter++; // Increase hash depth

                i = 0; // Restart insertion process
            } else {
                i++; // Move to next record
            }
        }
    }

    /*
     * Method writeHashIter()
     *
     * Purpose: Stores the current hash iteration value at the end
     *          of the index file for metadata purposes.
     * Pre-condition: Hash table has been populated.
     * Post-condition: hashIter value is written to the index file.
     */
    public void writeHashIter() throws IOException {
        // Write the hash iteration index at end of the buckets region
        indexFileStream.seek(getNumBuckets() * Consts.BUCKET_SIZE_BYTES);

        indexFileStream.writeInt(hashIter);
    }

    /*
     * Method displayBucketStatistics()
     *
     * Purpose:
     *   Computes and displays:
     *      1) Number of buckets
     *      2) Lowest bucket occupancy
     *      3) Highest bucket occupancy
     *      4) Mean and median bucket occupancy
     *
     * Pre-condition:
     *   Index has already been populated.
     *
     * Post-condition:
     *   Statistics are printed to standard output.
     */
    public void displayBucketStats() throws IOException {
        int numBuckets = getNumBuckets();
        int[] occupancies = new int[numBuckets];

        // Read occupancy (size field) of each bucket
        for (int i = 0; i < numBuckets; i++) {

            long bucketPtr = (long) i * Consts.BUCKET_SIZE_BYTES;
            indexFileStream.seek(bucketPtr);

            occupancies[i] = indexFileStream.readInt();
        }

        // Sort occupancies for the min, max, and median
        Arrays.sort(occupancies);

        int lowest = occupancies[0];
        int highest = occupancies[numBuckets - 1];

        // Compute the mean occupancy
        double sum = 0;
        for (int occ : occupancies)
            sum += occ;

        double mean = sum / numBuckets;

        // Compute the median occupancy
        double median;
        if (numBuckets % 2 == 0)
            median = (occupancies[numBuckets / 2 - 1] + occupancies[numBuckets / 2]) / 2.0;
        else
            median = occupancies[numBuckets / 2];

        // Display results
        System.out.println("Number of buckets: " + numBuckets);
        System.out.println("Lowest bucket occupancy: " + lowest);
        System.out.println("Highest bucket occupancy: " + highest);
        System.out.printf("Mean bucket occupancy: %.2f%n", mean);
        System.out.printf("Median bucket occupancy: %.2f%n", median);
    }


    /*
     * Method close()
     *
     * Purpose: Closes the underlying BinReader and RandomAccessFile streams.
     * Pre-condition: Streams are open and accessible.
     * Post-condition: All streams are safely closed.
     */
    public void close() throws IOException {
        if (binReader != null) binReader.close();
        if (indexFileStream != null) indexFileStream.close();
    }
   
}
