/*
 * Author: Tom Giallanza
 * Course: CSC 460 Database Design
 * Assignment: Program 2
 * Instructor: Dr. McCann
 * TAs: Jianwei Shen and Muhammad Bilal
 * Due Date: 02-12-2026
 *
 * Description:
 * This file defines the Record and RecordLengths classes, which together
 * represent and describe individual entries in the bat cave dataset. 
 *
 * The Record class is an immutable data object that stores all fields 
 * for a single dataset row, corresponding to a single bat cave observation. 
 * It provides read-only accessors for all stored fields, as well as static
 * comparators for sorting by Data.entry and distance from the equator. 
 * The class also includes utility methods for string normalization and 
 * formatted display of records.
 *
 * The RecordLengths class stores the maximum string lengths for each 
 * variable-length field across all dataset records. These values are used 
 * to define fixed-width binary record layouts when writing records to file.
 *
 * Responsibilities:
 * 1. Record stores immutable values for a single dataset entry.
 * 2. Provides getters for all fields.
 * 3. Provides comparators for sorting by entry or equatorial distance.
 * 4. Provides formatted string output with null/empty string handling.
 * 5. RecordLengths tracks maximum field sizes for binary serialization.
 *
 * Operational Requirements:
 *   Language: Java 25.0.1
 *   Compilation: javac Record.java
 *   Usage: Used by Prog1A and BinReader for CSV parsing and binary file handling
 *   Input: CSV dataset of bat cave records
 *   Output: Immutable Record objects with accessor methods
 */

import java.util.*;

/*
 * Class: Record
 * Author: Tom Giallanza
 * Purpose: An immutable data object that stores the field values of a
 *          single dataset record. Each Record represents one row from
 *          the input CSV file and provides read-only access to all
 *          stored fields.
 * Inherits From: None
 * Interfaces: None
 * Constants: None
 * Constructors: Record(String seqID, String entry, String series,
 *                      String realm, String continent, String biome,
 *                      String country, String cave,
 *                      double latitude, double longitude,
 *                      String species)
 * Class Methods: None
 * Inst. Methods: String getSeqID()
 *                String getEntry()
 *                String getSeries()
 *                String getRealm()
 *                String getContinent()
 *                String getBiome()
 *                String getCountry()
 *                String getCave()
 *                double getLatitude()
 *                double getLongitude()
 *                String getSpecies()
 */

class Record {
    private final String seqID;     // Dataset sequence ID
    private final String entry;     // Data.entry field
    private final String series;    // Data.series field
    private final String realm;     // Biogeographical.realm field
    private final String continent; // Continent field
    private final String biome;     // Biome classification field
    private final String country;   // Country.recod field
    private final String cave;      // Cave.site field
    private final double latitude;  // Latitude coordinate
    private final double longitude; // Longitude coordinate
    private final String species;   // Species.name field

    /*
     * Constructor: Record
     * Purpose: Creates a new immutable Record object containing all fields for
     *          one bat cave data entry.
     *
     * Pre-conditions: All string parameters should be non-null (may be empty for 
     *                 missing values) and latitude/longitude should be valid coordinates.
     *
     * Post-conditions: A new Record object is created with all fields initialized
     *                  and immutable after construction.
     *
     * Parameters:
     *   seqID: Dataset sequence ID
     *   entry: Data.entry field
     *   series: Data.series field
     *   realm: Biogeographical.realm field
     *   continent: Continent field
     *   biome: Biome classification field
     *   country: Country.record field
     *   cave: Cave.site field
     *   latitude: Latitude coordinate
     *   longitude: Longitude coordinate
     *   species: Species.name field
     */
    Record(String seqID, String entry, String series,
        String realm, String continent, String biome,
        String country, String cave,
        double latitude, double longitude,
        String species) {
        this.seqID = seqID;
        this.entry = entry;
        this.series = series;
        this.realm = realm;
        this.continent = continent;
        this.biome = biome;
        this.country = country;
        this.cave = cave;
        this.latitude = latitude;
        this.longitude = longitude;
        this.species = species;
    }

    /*
     * Method compareEntry
     *
     * Purpose: Compares two Record objects based on the numeric value of
     *          their Data.entry fields. This comparator is used to sort
     *          records in ascending order prior to writing them to the
     *          binary file and for ensuring correct search behavior.
     *
     * Pre-condition: Both Record objects contain valid numeric values
     *                in their Data.entry fields.
     * Post-condition: No Record objects are modified.
     * Returns: A negative integer, zero, or a positive integer if the
     *          first Record's entry value is less than, equal to, or
     *          greater than the second's.
     */
    public static Comparator<Record> compareEntry =
        (a, b) -> a.getEntry().compareTo(b.getEntry());

    /*
     * Method compareEqDist
     *
     * Purpose: Compares two Record objects based on their distance from
     *          the equator, calculated as the absolute value of the
     *          latitude field. Records are ordered from greatest to
     *          least distance.
     *
     * Pre-condition: Both Record objects contain valid latitude values.
     * Post-condition: No Record objects are modified.
     * Returns: A negative integer, zero, or a positive integer if the
     *          first Record should appear before, equal to, or after
     *          the second in descending equator distance order.
     */
    public static Comparator<Record> compareEqDist =
        (a, b) -> Double.compare(Math.abs(b.getLatitude()), Math.abs(a.getLatitude()));

    /*
     * The following getter methods retrieve the values of individual fields
     * from a Record object. All getters follow the same pattern:
     *
     * Pre-conditions: The Record object must be properly initialized
     * Post-conditions: The field value is returned unchanged
     * Parameters: None
     * Returns: The value of the specified field
     */
    public String getSeqID() { return seqID; }
    public String getEntry() { return entry; }
    public String getSeries() { return series; }
    public String getRealm() { return realm; }
    public String getContinent() { return continent; }
    public String getBiome() { return biome; }
    public String getCountry() { return country; }
    public String getCave() { return cave; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getSpecies() { return species; }

    /*
     * Method nullIfEmpty
     *
     * Purpose: Returns a string normalized to "null" if it is empty or blank.
     *          Otherwise, returns the string unchanged.
     * Pre-condition: Input string may be null or empty.
     * Post-condition: Original string is not modified.
     * Parameters:
     *   str - The input string to normalize
     * Returns: "null" for empty strings, original string otherwise.
     */
    public static String nullIfEmpty(String str) {
        if (str == null || str.trim().isEmpty())
            return String.format("%-" + Math.max(str.length(), 4) + "s", "null");
        else
            return str;
    }

    /*
     * Method padField
     *
     * Purpose: Returns a fixed-width string representation of the input string.
     *          Truncates if longer than width, pads with spaces if shorter,
     *          and formats null/empty strings as "null".
     * Pre-condition: Width parameter is provided by the caller.
     * Post-condition: Original string is not modified.
     * Parameters:
     *   str   - The input string to format
     *   width - The fixed field width in characters
     * Returns: Fixed-width string for display or binary writing.
     */
    public static String padField(String str, int width) {
        if (width <= 0) return "";

        if (str == null || str.trim().isEmpty())
            return String.format("%-" + Math.max(4, width) + "s", "    ");

        if (str.length() > width)
            str = str.substring(0, width);

        return String.format("%-" + width + "s", str);
    }

    /*
     * Method toString
     *
     * Purpose: Returns a formatted string representation of the Record
     *          using selected fields and padField formatting.
     * Pre-condition: Record object has been fully initialized.
     * Post-condition: Record fields remain unchanged.
     * Returns: Formatted string representing the Record.
     */
    public String toString() {
        return String.format(
            "[%s][%s][%s]", nullIfEmpty(padField(seqID, RecordLengths.maxSeqIDLen)),
                            nullIfEmpty(padField(country, RecordLengths.maxCountryLen)),
                            nullIfEmpty(padField(cave, RecordLengths.maxCaveLen))
        );
    }

}

/*
 * Class: RecordLengths
 * Author: Tom Giallanza
 * Purpose: A utility class that stores the maximum string lengths
 *          for each variable-length field across all records in
 *          the dataset. These values are computed while parsing
 *          the CSV file and are used to define the fixed-width
 *          binary record layout.
 *
 * Inherits From: None
 * Interfaces: None
 * Constants: None
 * Constructors: None
 * Class Methods: None
 * Inst. Methods: None
 */
class RecordLengths {
    static int maxSeqIDLen;             // Maximum length of Dataset sequence ID strings
    static int maxEntryLen;             // Maximum length of Data.entry strings
    static int maxSeriesLen;            // Maximum length of Data.series strings
    static int maxRealmLen;             // Maximum length of Biogeographical.realm strings
    static int maxContinentLen;         // Maximum length of Continent strings
    static int maxBiomeLen;             // Maximum length of Biome strings
    static int maxCountryLen;           // Maximum length of Country.record strings
    static int maxCaveLen;              // Maximum length of Cave.site strings
    static int maxSpeciesLen;           // Maximum length of Species.name strings
}
