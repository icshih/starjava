package uk.ac.starlink.pds4;

import gov.nasa.pds.label.object.FieldType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Pds4Test  {

    @Test
    public void testReaders() {
        String[] blankTxts = new String[ 0 ];
        for ( FieldType ftype : FieldType.values() ) {
            FieldReader rdr = FieldReader.getInstance( ftype, blankTxts );
            assertNotNull( rdr );
        }
    }
}
