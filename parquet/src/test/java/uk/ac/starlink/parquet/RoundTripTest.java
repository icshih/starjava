package uk.ac.starlink.parquet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.junit.jupiter.api.Test;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StoragePolicy;
import uk.ac.starlink.table.Tables;
import uk.ac.starlink.table.TestTableScheme;
import uk.ac.starlink.util.FileDataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class RoundTripTest {

    @Test
    public void testRoundTrip() throws IOException {
        StarTable table = new TestTableScheme().createTable( "100,s" );
        CompressionCodecName[] codecs = {
            null,
            CompressionCodecName.UNCOMPRESSED,
            CompressionCodecName.GZIP,
            CompressionCodecName.SNAPPY,
            CompressionCodecName.LZ4_RAW,
        };
        for ( CompressionCodecName codec : codecs ) {
            testRoundTrip( table, codec );
        }
        try {
            testRoundTrip( table, CompressionCodecName.LZ4 );
        } catch ( RuntimeException e ) {
            fail();
        }
    }

    private void testRoundTrip( StarTable table, CompressionCodecName codec )
            throws IOException {
        ParquetTableWriter writer = new ParquetTableWriter();
        writer.setCompressionCodec( codec );
        File f = File.createTempFile( "tmp-" + codec, "parquet" );
        f.deleteOnExit();
        try ( OutputStream out =
                  new BufferedOutputStream( new FileOutputStream( f ) ) ) {
            writer.writeStarTable( table, out );
        }
        StarTable table1 = new ParquetTableBuilder()
                          .makeStarTable( new FileDataSource( f ), false,
                           StoragePolicy.PREFER_MEMORY );
        assertSameTable( table, table1 );
        f.delete();
    }

    private void assertSameTable( StarTable t1, StarTable t2 ) {
        assertEquals( Tables.tableToString( t1, "csv" ),
                      Tables.tableToString( t2, "csv" ) );
    }
}
