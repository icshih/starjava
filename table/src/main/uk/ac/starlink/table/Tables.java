package uk.ac.starlink.table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for miscellaneous table-related functionality.
 */
public class Tables {

    /**
     * Returns a table based on a given table and guaranteed to have 
     * random access.  If the original table <tt>stab</tt> has random
     * access then it is returned, otherwise a new random access table
     * is built using its data.
     *
     * @param  stab  original table
     * @return  a table with the same data as <tt>startab</tt> and with 
     *          <tt>isRandom()==true</tt>
     */
    public static StarTable randomTable( StarTable startab )
            throws IOException {

        /* If it has random access already, we don't need to do any work. */
        if ( startab.isRandom() ) {
            return startab;
        }

        /* Otherwise, we need to construct a table based on the sequential
         * table that acts random. */
        return new RowScratchTable( startab );
    }

    /**
     * Convenience method to return an array of all the column headers
     * in a given table.  Modifying this array will not affect the table.
     *
     * @param  startab  the table being enquired about
     * @return an array of all the column headers
     */
    public static ColumnInfo[] getColumnInfos( StarTable startab ) {
        int ncol = startab.getColumnCount();
        ColumnInfo[] infos = new ColumnInfo[ ncol ];
        for ( int i = 0; i < ncol; i++ ) {
            infos[ i ] = startab.getColumnInfo( i );
        }
        return infos;
    }
}
