// The doc comments in this class are processed to produce user-visible
// documentation as part of the package build process.  For this reason
// care should be taken to make the doc comment style comprehensible,
// consistent, concise, and not over-technical.

package uk.ac.starlink.topcat.func;

/**
 * Specialist functions for use with data from the the 2QZ survey.
 * Spectral data are taken directly from the 2QZ web site at
 * <code>http://www.2dfquasar.org/"</code>.
 *
 * @author   Mark Taylor (Starlink)
 * @since    3 Sep 2004
 */
public class TwoQZ {

    /** String prepended to the object NAME for the FITS spectra file URL. */
    public static final String SPEC_BASE =
        "http://www.2dfquasar.org/fits/";

    /** String appended to the object NAME for the FITS spectra file URL. */
    public static final String SPEC_TAIL = ".fits.gz";

    /** String prepended to the object NAME for the FITS postage stamp URL. */
    public static final String FITS_IMAGE_BASE = 
        "http://www.2dfquasar.org/postfits/";

    /** String appended to the object NAME for the FITS postage stamp URL. */
    public static final String FITS_IMAGE_TAIL = ".fits.gz";

    /** String prepended to the object NAME for the JPEG postage stamp URL. */
    public static final String JPEG_IMAGE_BASE =
        "http://www.2dfquasar.org/postjpg/";

    /** String appended to the object NAME for the JPEG postage stamp URL. */
    public static final String JPEG_IMAGE_TAIL = ".jpg";

    /**
     * Private constructor prevents instantiation.
     */
    private TwoQZ() {
    }

    /**
     * Displays all the spectra relating to a 2QZ object in an external
     * viewer (SPLAT).
     *
     * @param   name  object name (NAME column)
     * @param   nobs  number of observations to display (NOBS column)
     * @return  short log message
     */
    public static String spectra2QZ( String name, int nobs ) {
        String[] locs = new String[ nobs ];
        String base = SPEC_BASE + getSubdir( name );
        for ( int i = 0; i < nobs; i++ ) {
            locs[ i ] = base + name + (char) ( 'a' + i ) + SPEC_TAIL;
        }
        return Splat.splatMulti( locs );
    }

    /**
     * Displays the postage stamp FITS image for a 2QZ object in an
     * external viewer (SoG).
     *
     * @param  name  object name (NAME column)
     * @return  short log message
     */
    public static String image2QZ( String name ) {
        String loc = FITS_IMAGE_BASE + getSubdir( name ) + name +
                     FITS_IMAGE_TAIL;
        return Sog.sog( loc );
    }

    /**
     * Displays the postage stamp JPEG image for a 2QZ object in an 
     * external viewer.
     *
     * @param  name  object name (NAME column)
     * @return  short log message
     */
    public static String jpeg2QZ( String name ) {
        String loc = JPEG_IMAGE_BASE + getSubdir( name ) + name +
                     JPEG_IMAGE_TAIL;
        return Image.displayImage( loc );
    }

    private static String getSubdir( String name ) {
        int rah = Integer.parseInt( name.substring( 1, 3 ) );
        int rah1 = rah + 1;
        return "ra" + format2( rah ) + '_' + format2( rah1 ) + '/';
    }

    private static String format2( int num ) {
        String out = Integer.toString( num );
        return out.length() == 2 ? out
                                 : "0" + out.charAt( 0 );
    }

}
