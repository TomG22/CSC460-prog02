/*
 * Author: Tom Giallanza
 * Course: CSC 460 Database Design
 * Assignment: Program 2
 * Instructor: Dr. McCann
 * TAs: Jianwei Shen and Muhammad Bilal
 * Due Date: 02-12-2026
 *
 * Description:
 * This file defines the BinReader class, which is responsible for reading,
 * interpreting, and querying a fixed-width binary file produced by Prog1A.
 * The binary file stores bat cave records in ascending order by the
 * Data.entry field and includes a footer containing maximum string lengths
 * for each variable-width field.
 *
 * The BinReader class performs the following responsibilities:
 * 1. Reads footer metadata to determine record structure and field sizes.
 * 2. Calculates the size and total number of records in the binary file.
 * 3. Reads individual records using random access.
 * 4. Provides record count and record size information.
 * 5. Closes the binary file stream safely.
 *
 * Operational Requirements:
 *   Language: Java 25.0.1
 *   Compilation: javac BinReader.java
 *   Usage: Instantiated by Prog1B
 *   Input: Binary file produced by Prog1A
 *   Output: Record objects and metadata for querying
 */

import java.io.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

/*
 * Class: BinReader
 * Author: Tom Giallanza
 * Purpose: An object of this class is responsible for reading and
 *          interpreting a fixed-width binary file produced by Prog1A.
 *          Each binary record corresponds to an immutable Record object,
 *          and the file includes a footer containing maximum string
 *          lengths required for correct parsing.
 * Inherits From: None
 * Interfaces: None
 * Constants: None
 * Constructors: BinReader(File binFilePath)
 * Class Methods: None
 * Inst. Methods: int getNumRecords()
 *                long getSizeOfRecord()
 *                void cacheLengths()
 *                Record readRecord(long recordPtr)
 *                void close()
 */
class BinReader {

    private File binFilePath;               // Binary file path
    private RandomAccessFile binFileStream; // Binary file stream
    private int numRecords;                 // Number of records in file
    private long recordSizeBytes;           // Size of one record in bytes

    /*
     * Method getNumRecords()
     *
     * Purpose: Returns the total number of records stored in the binary file.
     * Pre-condition: cacheLengths() has been executed.
     * Post-condition: No state is modified.
     * Returns: Total number of records.
     */
    public int getNumRecords() {
        return numRecords;
    }

    /*
     * Method getSizeOfRecord()
     *
     * Purpose: Returns the size in bytes of a single fixed-width record.
     * Pre-condition: cacheLengths() has been executed.
     * Post-condition: No state is modified.
     * Returns: Size of one record in bytes.
     */
    public long getSizeOfRecord() {
        return recordSizeBytes;
    }

    /*
     * Constructor BinReader(binFilePath)
     *
     * Purpose: Initializes a BinReader object and loads footer metadata
     *          from the specified binary file into memory.
     * Pre-condition: binFilePath references a valid, readable binary file.
     * Post-condition: Footer metadata is cached and record size calculated.
     * Parameters: binFilePath - Reference to the binary file.
     */
    public BinReader(File binFilePath) throws IOException {
        this.binFilePath = binFilePath; // Store binary file reference
        binFileStream = new RandomAccessFile(this.binFilePath, "r"); // Open file

        cacheLengths(); // Load metadata from footer
    }

    /*
     * Method cacheLengths()
     *
     * Purpose: Reads the footer of the binary file to retrieve maximum
     *          string lengths for each variable-width field. Using this
     *          information, it computes the size of each record and the
     *          total number of records in the file.
     * Pre-condition: The binary file exists and follows the expected format.
     * Post-condition: RecordLengths values are populated and record size
     *                 and record count are computed.
     */
    public void cacheLengths() throws IOException {
        long binFileSize = binFilePath.length(); // Total file size in bytes
        int footerSize = Integer.BYTES * 9;      // Footer contains 9 integers

        // If the binary file is improperly formatted or empty, set the
        // lengths and record size and quantity to 0 and early return
        if (binFileSize <= footerSize) {
            RecordLengths.maxSeqIDLen     = 0;
            RecordLengths.maxEntryLen     = 0;
            RecordLengths.maxSeriesLen    = 0;
            RecordLengths.maxRealmLen     = 0;
            RecordLengths.maxContinentLen = 0;
            RecordLengths.maxBiomeLen     = 0;
            RecordLengths.maxCountryLen   = 0;
            RecordLengths.maxCaveLen      = 0;
            RecordLengths.maxSpeciesLen   = 0;

            recordSizeBytes = 0;
            numRecords = 0;

            return;
        }

        // Seek to footer location
        binFileStream.seek(binFileSize - footerSize);

        // Read maximum string lengths
        RecordLengths.maxSeqIDLen     = binFileStream.readInt();
        RecordLengths.maxEntryLen     = binFileStream.readInt();
        RecordLengths.maxSeriesLen    = binFileStream.readInt();
        RecordLengths.maxRealmLen     = binFileStream.readInt();
        RecordLengths.maxContinentLen = binFileStream.readInt();
        RecordLengths.maxBiomeLen     = binFileStream.readInt();
        RecordLengths.maxCountryLen   = binFileStream.readInt();
        RecordLengths.maxCaveLen      = binFileStream.readInt();
        RecordLengths.maxSpeciesLen   = binFileStream.readInt();

        // Compute total record size in bytes
        recordSizeBytes =
            RecordLengths.maxSeqIDLen +
            RecordLengths.maxEntryLen +
            RecordLengths.maxSeriesLen +
            RecordLengths.maxRealmLen +
            RecordLengths.maxContinentLen +
            RecordLengths.maxBiomeLen +
            RecordLengths.maxCountryLen +
            RecordLengths.maxCaveLen +
            Double.BYTES * 2 +
            RecordLengths.maxSpeciesLen;

        // Compute number of records stored before footer
        numRecords = (int) ((binFileSize - footerSize) / recordSizeBytes);
    }

    /*
     * Method readString(len)
     *
     * Purpose: Reads a fixed-length ASCII string from the current
     *          file position.
     * Pre-condition: The file pointer is positioned at the start of
     *                the string to read.
     * Post-condition: The file pointer advances by len bytes.
     * Parameters: len - Number of bytes to read.
     * Returns: String containing the read characters.
     */
    private String readString(int len) throws IOException {
        if (len == 0)
            return ""; // No bytes to read

        byte[] bytes = new byte[len];
        binFileStream.readFully(bytes); // Read exact number of bytes

        return new String(bytes, StandardCharsets.US_ASCII);
    }

    /*
     * Method readRecord(recordPtr)
     *
     * Purpose: Reads a single record at the specified byte offset and
     *          returns it as an immutable Record object.
     * Pre-condition: recordPtr references a valid record location.
     * Post-condition: The file pointer advances by one record.
     * Parameters: recordPtr - Byte offset of record in file.
     * Returns: Populated Record object.
     */
    public Record readRecord(long recordPtr) throws IOException {
        if (binFilePath.length() == 0) return null;

        if (recordPtr < 0 || recordPtr >= numRecords * recordSizeBytes) {
            throw new IOException(
                "Attempted to read record at invalid pointer: " + recordPtr);
        }

        // Seek to beginning of record
        binFileStream.seek(recordPtr);

        // Construct Record from fixed-width fields
        return new Record(
            readString(RecordLengths.maxSeqIDLen),
            readString(RecordLengths.maxEntryLen),
            readString(RecordLengths.maxSeriesLen),
            readString(RecordLengths.maxRealmLen),
            readString(RecordLengths.maxContinentLen),
            readString(RecordLengths.maxBiomeLen),
            readString(RecordLengths.maxCountryLen),
            readString(RecordLengths.maxCaveLen),
            binFileStream.readDouble(),
            binFileStream.readDouble(),
            readString(RecordLengths.maxSpeciesLen)
        );
    }

    /*
     * Method close()
     *
     * Purpose: Closes the binary file stream and releases system resources.
     * Pre-condition: The file stream is open.
     * Post-condition: The file stream is closed.
     */
    public void close() throws IOException {
        if (binFileStream != null) binFileStream.close();
    }
}
