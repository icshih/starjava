package uk.ac.starlink.cdf;

/**
 * Encapsulates choices about how to do the conversion from a CDF to a
 * StarTable.
 *
 * @author   Mark Taylor
 * @since    24 Jun 2013
 * @see   CdfTableBuilder
 */
public interface CdfTableProfile {

    /**
     * Determines whether CDF variables with fixed (non-record-varying)
     * values are turned into columns or parameters in the StarTable.
     *
     * @return   if true, non-record-varying variables will be table parameters,
     *           if false they will be table columns with the same value
     *           in each row
     */
    boolean invariantVariablesToParameters();

    /**
     * Returns the name of the CDF Variable Attribute whose value is used
     * to supply the column description metadata for the converted StarTable.
     *
     * @param  attNames  names of all the variable attributes present in the CDF
     * @return   attribute name for description; if null or not present,
     *           column has no description
     * @see  uk.ac.starlink.table.ValueInfo#getDescription 
     */
    String getDescriptionAttribute( String[] attNames );

    /**
     * Returns the name of the CDF Variable Attribute whose value is used
     * to supply the column units metadata for the converted StarTable.
     *
     * @param  attNames  names of all the variable attributes present in the CDF
     * @return   attribute name for unit string; if null or not present,
     *           column has no units
     * @see   uk.ac.starlink.table.ValueInfo#getUnitString
     */
    String getUnitAttribute( String[] attNames );

    /**
     * 
     * Returns the name of the CDF Variable Attribute whose value is used
     * to specify magic blank values for the variable data.
     * This is typically "FILLVAL".
     *
     * @param  attNames  names of all the variable attributes present in the CDF
     * @return   attribute name for blank avlue; if null or not present,
     *           column has no magic blank value
     */
    String getBlankValueAttribute( String[] attNames );
}
