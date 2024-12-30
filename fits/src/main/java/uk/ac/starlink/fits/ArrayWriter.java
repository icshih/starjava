package uk.ac.starlink.fits;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.logging.Logger;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.Tables;

/**
 * Object which knows how to write array data for a particular type 
 * to a FITS BINTABLE extension.
 *
 * @author   Mark Taylor
 * @since    10 Jul 2008
 */
abstract class ArrayWriter {

    private final char formatChar_;
    private final int nByte_;

    private static final Logger logger_ =
        Logger.getLogger( "uk.ac.starlink.fits" );

    /**
     * Constructor.
     *
     * @param  formatChar  data type-specific TFORM character
     * @param  nByte   number of bytes for each element written
     */
    protected ArrayWriter( char formatChar, int nByte ) {
        formatChar_ = formatChar;
        nByte_ = nByte;
    }

    /**
     * Returns the type-specific TFORM format character for this writer.
     *
     * @return  format character
     */
    public char getFormatChar() {
        return formatChar_;
    }

    /**
     * Returns the number of bytes for each element written by this writer.
     *
     * @return  byte count written
     */
    public int getByteCount() {
        return nByte_;
    }

    /**
     * Writes an element of an array to an output stream.
     *
     * @param  out  output stream
     * @param  array  array to take value from, of type appropriate for this
     *                writer
     * @param  index  index of element to write
     */
    public abstract void writeElement( DataOutput out, Object array,
                                       int index )
            throws IOException;

    /**
     * Writes a padding value to an output stream.
     *
     * @param  out  destination stream
     */
    public abstract void writePad( DataOutput out ) throws IOException;

    /**
     * Returns offset value for this writer (normally 0).
     *
     * @return BZERO value
     */
    public abstract BigDecimal getZero();

    /**
     * Constructs a new ArrayWriter for a given array class.
     *
     * @param   cinfo   column metadata describing the data
     *                  which this writer should be able to write
     * @param   allowSignedByte  if true, bytes written as FITS signed bytes
     *          (TZERO=-128), if false bytes written as signed shorts
     * @return  new ArrayWriter, or null if <code>cinfo</code> can't be handled
     */
    public static ArrayWriter createArrayWriter( ColumnInfo cinfo,
                                                 boolean allowSignedByte ) {
        Class<?> clazz = cinfo.getContentClass();
        final boolean isUbyte =
            Boolean.TRUE
           .equals( cinfo.getAuxDatumValue( Tables.UBYTE_FLAG_INFO,
                                            Boolean.class ) );
        final BigInteger longOffset =
            ScalarColumnWriter.getLongOffset( cinfo );

        if ( isUbyte ) {
            if ( clazz == short[].class ) {
                return new NormalArrayWriter( 'B', 1,
                                              new short[] { (short) 0 } ) {
                    public void writeElement( DataOutput out, Object array,
                                              int ix )
                            throws IOException {
                        out.writeByte( ((short[]) array)[ ix ] );
                    }
                };
            }
            else {
                logger_.warning( "Ignoring " + Tables.UBYTE_FLAG_INFO
                               + " on non-short[] column " + cinfo );
            }
        }
        if ( longOffset != null ) {
            if ( clazz == String[].class ) {
                final BigDecimal zeroNum = new BigDecimal( longOffset );
                final long badVal = Long.MIN_VALUE;
                return new ArrayWriter( 'K', 8 ) {
                    public void writeElement( DataOutput out, Object array,
                                              int ix )
                            throws IOException {
                        String sval = ((String[]) array)[ ix ];
                        long lval =
                            ScalarColumnWriter
                           .getOffsetLongValue( sval, longOffset, badVal );
                        out.writeLong( lval );
                    }
                    public void writePad( DataOutput out ) throws IOException {
                        out.writeLong( 0L );
                    }
                    public BigDecimal getZero() {
                        return zeroNum;
                    }
                };
            }
            else {
                logger_.warning( "Ignoring " + BintableStarTable.LONGOFF_INFO
                               + " on non-String[] column " + cinfo );
            }
        }
        if ( clazz == boolean[].class ) {
            return new ArrayWriter( 'L', 1 ) {
                public void writeElement( DataOutput out, Object array, int ix )
                        throws IOException {
                    out.writeByte( ((boolean[]) array)[ ix ] ? (byte) 'T'
                                                             : (byte) 'F' );
                }
                public void writePad( DataOutput out ) throws IOException {
                    out.writeByte( (byte) 0 );
                }
                public BigDecimal getZero() {
                    return BigDecimal.ZERO;
                }
            };
        }
        else if ( clazz == byte[].class ) {
            if ( allowSignedByte ) {
                final BigDecimal zeroByte = new BigDecimal( -128 );
                return new NormalArrayWriter( 'B', 1,
                                              new byte[] { (byte) 0 } ) {
                    public void writeElement( DataOutput out, Object array,
                                              int ix )
                            throws IOException {
                        out.writeByte( ((byte[]) array)[ ix ] ^ (byte) 0x80 );
                    }
                    public BigDecimal getZero() {
                        return zeroByte;
                    }
                };
            }
            else {
                return new NormalArrayWriter( 'I', 2,
                                              new byte[] { (byte) 0 } ) {
                    public void writeElement( DataOutput out, Object array,
                                              int ix )
                            throws IOException {
                        out.writeShort( (short) ((byte[]) array)[ ix ] );
                    }
                };
            }
        }
        else if ( clazz == short[].class ) {
            return new NormalArrayWriter( 'I', 2, new short[] { (short) 0 } ) {
                public void writeElement( DataOutput out, Object array, int ix )
                        throws IOException {
                    out.writeShort( ((short[]) array)[ ix ] );
                }
            };
        }
        else if ( clazz == int[].class ) {
            return new NormalArrayWriter( 'J', 4, new int[] { 0 } ) {
                public void writeElement( DataOutput out, Object array, int ix )
                        throws IOException {
                    out.writeInt( ((int[]) array)[ ix ] );
                }
            };
        }
        else if ( clazz == long[].class ) {
            return new NormalArrayWriter( 'K', 8, new long[] { 0L } ) {
                public void writeElement( DataOutput out, Object array, int ix )
                        throws IOException {
                    out.writeLong( ((long[]) array)[ ix ] );
                }
            };
        }
        else if ( clazz == float[].class ) {
            return new NormalArrayWriter( 'E', 4, new float[] { Float.NaN } ) {
                public void writeElement( DataOutput out, Object array, int ix )
                        throws IOException {
                    out.writeFloat( ((float[]) array)[ ix ] );
                }
            };
        }
        else if ( clazz == double[].class ) {
            return new NormalArrayWriter( 'D', 8,
                                          new double[] { Double.NaN } ) {
                public void writeElement( DataOutput out, Object array, int ix )
                        throws IOException {
                    out.writeDouble( ((double[]) array)[ ix ] );
                }
            };
        }

        /* Not an array. */
        else {
            return null;
        }
    }

    /**
     * ArrayWriter implmentation suitable for most cases.
     */
    private static abstract class NormalArrayWriter extends ArrayWriter {
        private final Object pad1_;

        /**
         * Constructor.
         *
         * @param  formatChar  format character
         * @param  nByte   byte count
         * @param  pad1  1-element array containing a padding value
         */
        protected NormalArrayWriter( char formatChar, int nByte, Object pad1 ) {
            super( formatChar, nByte );
            pad1_ = pad1;
            if ( Array.getLength( pad1 ) != 1 ) {
                throw new IllegalArgumentException();
            }
        }

        public void writePad( DataOutput out ) throws IOException {
            writeElement( out, pad1_, 0 );
        }

        public BigDecimal getZero() {
            return BigDecimal.ZERO;
        }
    }
}
