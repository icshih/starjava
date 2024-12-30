package uk.ac.starlink.table;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SchemeTest {

    @Test
    public void testSchemes() throws IOException {
        StarTableFactory tfact = new StarTableFactory();
        Map<String,TableScheme> schemes = tfact.getSchemes();
        assertEquals(
            new HashSet<String>( Arrays.asList( "jdbc", "class", "loop",
                                                "test", "hapi" ) ),
                      schemes.keySet() );
        failCreateTable( tfact, ":num:10" );
        tfact.addScheme( new NumTableScheme() );
        failCreateTable( tfact, ":num:ten" );
        StarTable tnum = createTable( tfact, ":num:10" );
        Assertions.assertEquals( 1, tnum.getColumnCount() );
        for ( int i = 0; i < 10; i++ ) {
            Assertions.assertEquals( i, tnum.getCell( i, 0 ) );
        }
        tfact.getSchemes().remove( "num" );
        failCreateTable( tfact, ":num:ten" );
    }

    @Test
    public void testLoop() throws IOException {
        StarTableFactory tfact = new StarTableFactory();
        StarTable tloop = createTable( tfact, ":loop:10" );
        failCreateTable( tfact, ":loop:" );
        failCreateTable( tfact, ":loop:a,b,c" );
        failCreateTable( tfact, ":loop:one" );
        StarTable t1 = createTable( tfact, ":loop:10" );
        StarTable t2 = createTable( tfact, ":loop:1000,1010" );
        StarTable t3 = createTable( tfact, ":loop:0,100,10" );
        for ( int i = 0; i < 10; i++ ) {
            Assertions.assertEquals( i, t1.getCell( i, 0 ) );
            Assertions.assertEquals( i + 1000, t2.getCell( i, 0 ) );
            Assertions.assertEquals( i * 10, t3.getCell( i, 0 ) );
        }
        tfact.getSchemes().remove( "loop" );
        failCreateTable( tfact, ":loop:10" );
    }

    @Test
    public void testClazz() throws IOException {
        StarTableFactory tfact = new StarTableFactory();
        failCreateTable( tfact, ":class:java.lang.String:10" );
        failCreateTable( tfact,
                         ":class:uk.ac.starlink.table.LoopTableScheme:do_what");
        String l10spec = ":class:uk.ac.starlink.table.LoopTableScheme:10";
        StarTable tcloop = createTable( tfact, l10spec );
        Assertions.assertEquals( 1, tcloop.getColumnCount() );
        Assertions.assertEquals( 10, tcloop.getRowCount() );
        tfact.getSchemes().remove( "class" );
        failCreateTable( tfact, l10spec );
    }

    public void testTest() throws IOException {
        StarTableFactory tfact = new StarTableFactory();
        failCreateTable( tfact, ":test:xx" );
        failCreateTable( tfact, ":test:10,X" );
        failCreateTable( tfact, ":test:100,is,99" );
 
        StarTable iTable = createTable( tfact, ":test:99,i" );
        Assertions.assertEquals( 99, iTable.getRowCount() );
        Assertions.assertEquals( 1, iTable.getColumnCount() );

        assertTrue( createTable( tfact, ":test:10000" ).getColumnCount() >= 3 );

        assertNull( createTable( tfact, ":test:10,s" )
                   .getColumnInfo( 0 )
                   .getContentClass()
                   .getComponentType() );
        assertNotNull( createTable( tfact, ":test:10,f" )
                      .getColumnInfo( 0 )
                      .getContentClass()
                      .getComponentType() );
        assertNotNull( createTable( tfact, ":test:10,v" )
                      .getColumnInfo( 0 )
                      .getContentClass()
                      .getComponentType() );

        StarTable t1k = createTable( tfact, ":test:1000,*" );
        Assertions.assertEquals( 1000, t1k.getRowCount() );
        assertTrue( t1k.getColumnCount() >= 30 );
        Tables.checkTable( t1k );
    }

    private StarTable createTable( StarTableFactory tfact, String tspec )
            throws IOException {
        StarTable table = tfact.makeStarTable( tspec );
        Tables.checkTable( table );
        return table;
    }

    private void failCreateTable( StarTableFactory tfact, String tspec ) {
        try {
            tfact.makeStarTable( tspec );
            fail();
        }
        catch ( IOException e ) {
            return;
        }
    }

    private static class NumTableScheme implements TableScheme {
        public String getSchemeName() {
            return "num";
        }
        public String getSchemeUsage() {
            return "<number>";
        }
        public String getExampleSpecification() {
            return null;
        }
        public StarTable createTable( String spec ) throws IOException {
            int num;
            try {
                num = Integer.parseInt( spec );
            }
            catch ( NumberFormatException e ) {
                throw new IOException( "Not a number: " + spec );
            }
            ColumnStarTable table = ColumnStarTable.makeTableWithRows( num );
            int[] data = new int[ num ];
            for ( int i = 0; i < num; i++ ) {
                data[ i ] = i;
            }
            table.addColumn( ArrayColumn.makeColumn( "data", data ) );
            return table;
        }
    }
}
