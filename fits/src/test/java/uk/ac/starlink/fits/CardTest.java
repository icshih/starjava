package uk.ac.starlink.fits;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CardTest {

    @Test
    public void testCardFactory() {
        CardImage end = CardFactory.END_CARD;
        String end80 = pad80( "END" );
        assertEquals( end80, end.toString() );
        end.getBytes()[ 23 ] = (byte) 'X';
        assertEquals( end80, end.toString() );
    }

    private static String pad80( String txt ) {
        StringBuffer buf80 = new StringBuffer( 80 );
        buf80.append( txt );
        while ( buf80.length() < 80 ) {
            buf80.append( ' ' );
        }
        assertEquals( 80, buf80.length() );
        return buf80.toString();
    }
}
