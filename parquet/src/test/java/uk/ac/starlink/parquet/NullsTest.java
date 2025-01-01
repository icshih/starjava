package uk.ac.starlink.parquet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import uk.ac.starlink.table.ArrayColumn;
import uk.ac.starlink.table.ColumnStarTable;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StoragePolicy;
import uk.ac.starlink.table.Tables;
import uk.ac.starlink.util.FileDataSource;

import static org.junit.jupiter.api.Assertions.*;

public class NullsTest {

    final static Logger logger = LoggerFactory.getLogger( NullsTest.class );

    @Test
    public void testRun() throws IOException {
        int nrow = 4;
        double[] scalarData = new double[] { 1, 2, Double.NaN, 4 };
        double[][] vectorData = new double[][] {
            new double[] { 11, 12, 13 },
            new double[] { 21, Double.NaN, 23 },
            new double[] { Double.NaN, 32, 33 },
            null,
        };
        ColumnStarTable t1 = ColumnStarTable.makeTableWithRows( nrow );
        t1.addColumn( ArrayColumn.makeColumn( "scalar", scalarData ) );
        t1.addColumn( ArrayColumn.makeColumn( "vector", vectorData ) );
        File file = File.createTempFile( "ptest", ".parquet" );
        file.deleteOnExit();
        ParquetTableWriter writer = new ParquetTableWriter();
        try ( OutputStream out = new FileOutputStream( file ) ) {
            writer.writeStarTable( t1, out );
        }
        StarTable t2 =
            new ParquetTableBuilder()
           .makeStarTable( new FileDataSource( file ), false,
                           StoragePolicy.PREFER_MEMORY );
        t2 = Tables.randomTable( t2 );
        for ( int irow = 0; irow < nrow; irow++ ) {
            assertEquals( t1.getCell( irow, 0 ), t2.getCell( irow, 0 ) );
            assertArrayEquals( new Object[]{t1.getCell( irow, 1 )},
                    new Object[]{t2.getCell( irow, 1 )} );
        }
        Tables.checkTable( t2 );
        file.delete();

        assertTrue( writer.isGroupArray() );
        writer.setGroupArray( false );
        try ( OutputStream out = new FileOutputStream( file ) ) {
            writer.writeStarTable( t1, out );
            fail();
        }
        catch ( IOException e ) {
            assertTrue( e.getCause()
                        instanceof NullsWithoutGroupArrayException );
        }

        file.delete();
    }
}
