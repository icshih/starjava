package uk.ac.starlink.table;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class InfoTest {

    @Test
    public void testUtype() {
        ValueInfo colInfo = new ColumnInfo( "ABV", Double.class, "Strength" );

        DefaultValueInfo info = new DefaultValueInfo( "ABV", Double.class,
                                                      "Strength" );
        assertNull( info.getUtype() );
        info.setUtype( "meta.weird" );
        assertEquals( "meta.weird", info.getUtype() );
    }

    @Test
    public void testUCD() {
        ColumnInfo colInfo = new ColumnInfo( "ABV", Double.class, "Strength" );
        assertNull( colInfo.getUCD() );
        colInfo.setUCD( "meta.weird" );
        assertEquals( "meta.weird", colInfo.getUCD() );
        DefaultValueInfo info = new DefaultValueInfo( "ABV", Double.class,
                                                      "Strength" );
        assertNull( info.getUCD() );
        info.setUCD( "meta.weird" );
        assertEquals( "meta.weird", info.getUCD() );
    }
}
