
/*
 * ***************************************************************************
 * ** NOTE: Do not edit; this java source was written by WriteConvertWorker **
 * ***************************************************************************
 */

package uk.ac.starlink.array;

abstract class ConvertWorker {

    abstract boolean isUnit();
    abstract void convert( Object src, int srcPos,
                           Object dest, int destPos, int length );

    private static final byte MIN_BYTE = (byte) 0x81;
    private static final short MIN_SHORT = (short) 0x8001;
    private static final int MIN_INT = 0x80000001;
    private static final float MIN_FLOAT = -Float.MAX_VALUE;
    private static final double MIN_DOUBLE = -Double.MAX_VALUE;

    private static final byte MAX_BYTE = (byte) 0x7f;
    private static final short MAX_SHORT = (short) 0x7fff;
    private static final int MAX_INT = 0x7fffffff;
    private static final float MAX_FLOAT = Float.MAX_VALUE;
    private static final double MAX_DOUBLE = Double.MAX_VALUE;

    private static abstract class NonunitConvertWorker extends ConvertWorker {
        private final BadHandler destHandler;
        NonunitConvertWorker( BadHandler destHandler ) {
            this.destHandler = destHandler;
        }
        final boolean isUnit() {
            return false;
        }
    }

    static ConvertWorker makeWorker( Type sType, BadHandler sHandler,
                                     Type dType, BadHandler dHandler ) {
        return makeWorker( sType, sHandler, dType, dHandler, null );
    }

    static ConvertWorker makeWorker( Type sType, final BadHandler sHandler,
                                     Type dType, final BadHandler dHandler,
                                     final Mapper mapper ) {
        if ( false ) {
            return null;
            // dummy clause
        }
        else if ( sType == Type.BYTE && dType == Type.BYTE &&
                sHandler.equals( dHandler ) &&
                mapper == null ) {
            return new ConvertWorker() {
                final boolean isUnit() {
                    return true;
                }
                final void convert( Object src, int srcPos,
                                    Object dest, int destPos, int length ) {
                    System.arraycopy( src, srcPos, dest, destPos, length );
                }
            };
        }
        else if ( sType == Type.BYTE && dType == Type.BYTE ) {
            byte[] destbad = (byte[]) Type.BYTE.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final byte dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        byte[] src = (byte[]) source;
                        byte[] dest = (byte[]) destination;
                        while ( length-- > 0 ) {
                            boolean isBad = sHandler.isBad( source, srcPos );
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (byte) src[ srcPos ];
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        byte[] src = (byte[]) source;
                        byte[] dest = (byte[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_BYTE
                                        || dvald > (double) MAX_BYTE;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (byte) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.BYTE && dType == Type.SHORT ) {
            short[] destbad = (short[]) Type.SHORT.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final short dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        byte[] src = (byte[]) source;
                        short[] dest = (short[]) destination;
                        while ( length-- > 0 ) {
                            boolean isBad = sHandler.isBad( source, srcPos );
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (short) src[ srcPos ];
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        byte[] src = (byte[]) source;
                        short[] dest = (short[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_SHORT
                                        || dvald > (double) MAX_SHORT;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (short) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.BYTE && dType == Type.INT ) {
            int[] destbad = (int[]) Type.INT.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final int dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        byte[] src = (byte[]) source;
                        int[] dest = (int[]) destination;
                        while ( length-- > 0 ) {
                            boolean isBad = sHandler.isBad( source, srcPos );
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (int) src[ srcPos ];
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        byte[] src = (byte[]) source;
                        int[] dest = (int[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_INT
                                        || dvald > (double) MAX_INT;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (int) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.BYTE && dType == Type.FLOAT ) {
            float[] destbad = (float[]) Type.FLOAT.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final float dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        byte[] src = (byte[]) source;
                        float[] dest = (float[]) destination;
                        while ( length-- > 0 ) {
                            boolean isBad = sHandler.isBad( source, srcPos );
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (float) src[ srcPos ];
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        byte[] src = (byte[]) source;
                        float[] dest = (float[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_FLOAT
                                        || dvald > (double) MAX_FLOAT;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (float) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.BYTE && dType == Type.DOUBLE ) {
            double[] destbad = (double[]) Type.DOUBLE.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final double dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        byte[] src = (byte[]) source;
                        double[] dest = (double[]) destination;
                        while ( length-- > 0 ) {
                            boolean isBad = sHandler.isBad( source, srcPos );
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (double) src[ srcPos ];
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        byte[] src = (byte[]) source;
                        double[] dest = (double[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_DOUBLE
                                        || dvald > (double) MAX_DOUBLE;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (double) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.SHORT && dType == Type.SHORT &&
                sHandler.equals( dHandler ) &&
                mapper == null ) {
            return new ConvertWorker() {
                final boolean isUnit() {
                    return true;
                }
                final void convert( Object src, int srcPos,
                                    Object dest, int destPos, int length ) {
                    System.arraycopy( src, srcPos, dest, destPos, length );
                }
            };
        }
        else if ( sType == Type.SHORT && dType == Type.BYTE ) {
            byte[] destbad = (byte[]) Type.BYTE.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final byte dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        short[] src = (short[]) source;
                        byte[] dest = (byte[]) destination;
                        while ( length-- > 0 ) {
                            short sval = src[ srcPos ];
                            boolean isBad = sHandler.isBad( source, srcPos )
                                    || sval < MIN_BYTE
                                    || sval > MAX_BYTE;
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (byte) sval;
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        short[] src = (short[]) source;
                        byte[] dest = (byte[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_BYTE
                                        || dvald > (double) MAX_BYTE;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (byte) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.SHORT && dType == Type.SHORT ) {
            short[] destbad = (short[]) Type.SHORT.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final short dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        short[] src = (short[]) source;
                        short[] dest = (short[]) destination;
                        while ( length-- > 0 ) {
                            boolean isBad = sHandler.isBad( source, srcPos );
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (short) src[ srcPos ];
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        short[] src = (short[]) source;
                        short[] dest = (short[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_SHORT
                                        || dvald > (double) MAX_SHORT;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (short) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.SHORT && dType == Type.INT ) {
            int[] destbad = (int[]) Type.INT.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final int dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        short[] src = (short[]) source;
                        int[] dest = (int[]) destination;
                        while ( length-- > 0 ) {
                            boolean isBad = sHandler.isBad( source, srcPos );
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (int) src[ srcPos ];
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        short[] src = (short[]) source;
                        int[] dest = (int[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_INT
                                        || dvald > (double) MAX_INT;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (int) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.SHORT && dType == Type.FLOAT ) {
            float[] destbad = (float[]) Type.FLOAT.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final float dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        short[] src = (short[]) source;
                        float[] dest = (float[]) destination;
                        while ( length-- > 0 ) {
                            boolean isBad = sHandler.isBad( source, srcPos );
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (float) src[ srcPos ];
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        short[] src = (short[]) source;
                        float[] dest = (float[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_FLOAT
                                        || dvald > (double) MAX_FLOAT;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (float) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.SHORT && dType == Type.DOUBLE ) {
            double[] destbad = (double[]) Type.DOUBLE.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final double dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        short[] src = (short[]) source;
                        double[] dest = (double[]) destination;
                        while ( length-- > 0 ) {
                            boolean isBad = sHandler.isBad( source, srcPos );
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (double) src[ srcPos ];
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        short[] src = (short[]) source;
                        double[] dest = (double[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_DOUBLE
                                        || dvald > (double) MAX_DOUBLE;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (double) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.INT && dType == Type.INT &&
                sHandler.equals( dHandler ) &&
                mapper == null ) {
            return new ConvertWorker() {
                final boolean isUnit() {
                    return true;
                }
                final void convert( Object src, int srcPos,
                                    Object dest, int destPos, int length ) {
                    System.arraycopy( src, srcPos, dest, destPos, length );
                }
            };
        }
        else if ( sType == Type.INT && dType == Type.BYTE ) {
            byte[] destbad = (byte[]) Type.BYTE.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final byte dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        int[] src = (int[]) source;
                        byte[] dest = (byte[]) destination;
                        while ( length-- > 0 ) {
                            int sval = src[ srcPos ];
                            boolean isBad = sHandler.isBad( source, srcPos )
                                    || sval < MIN_BYTE
                                    || sval > MAX_BYTE;
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (byte) sval;
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        int[] src = (int[]) source;
                        byte[] dest = (byte[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_BYTE
                                        || dvald > (double) MAX_BYTE;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (byte) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.INT && dType == Type.SHORT ) {
            short[] destbad = (short[]) Type.SHORT.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final short dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        int[] src = (int[]) source;
                        short[] dest = (short[]) destination;
                        while ( length-- > 0 ) {
                            int sval = src[ srcPos ];
                            boolean isBad = sHandler.isBad( source, srcPos )
                                    || sval < MIN_SHORT
                                    || sval > MAX_SHORT;
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (short) sval;
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        int[] src = (int[]) source;
                        short[] dest = (short[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_SHORT
                                        || dvald > (double) MAX_SHORT;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (short) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.INT && dType == Type.INT ) {
            int[] destbad = (int[]) Type.INT.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final int dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        int[] src = (int[]) source;
                        int[] dest = (int[]) destination;
                        while ( length-- > 0 ) {
                            boolean isBad = sHandler.isBad( source, srcPos );
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (int) src[ srcPos ];
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        int[] src = (int[]) source;
                        int[] dest = (int[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_INT
                                        || dvald > (double) MAX_INT;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (int) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.INT && dType == Type.FLOAT ) {
            float[] destbad = (float[]) Type.FLOAT.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final float dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        int[] src = (int[]) source;
                        float[] dest = (float[]) destination;
                        while ( length-- > 0 ) {
                            boolean isBad = sHandler.isBad( source, srcPos );
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (float) src[ srcPos ];
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        int[] src = (int[]) source;
                        float[] dest = (float[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_FLOAT
                                        || dvald > (double) MAX_FLOAT;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (float) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.INT && dType == Type.DOUBLE ) {
            double[] destbad = (double[]) Type.DOUBLE.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final double dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        int[] src = (int[]) source;
                        double[] dest = (double[]) destination;
                        while ( length-- > 0 ) {
                            boolean isBad = sHandler.isBad( source, srcPos );
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (double) src[ srcPos ];
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        int[] src = (int[]) source;
                        double[] dest = (double[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_DOUBLE
                                        || dvald > (double) MAX_DOUBLE;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (double) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.FLOAT && dType == Type.FLOAT &&
                sHandler.equals( dHandler ) &&
                mapper == null ) {
            return new ConvertWorker() {
                final boolean isUnit() {
                    return true;
                }
                final void convert( Object src, int srcPos,
                                    Object dest, int destPos, int length ) {
                    System.arraycopy( src, srcPos, dest, destPos, length );
                }
            };
        }
        else if ( sType == Type.FLOAT && dType == Type.BYTE ) {
            byte[] destbad = (byte[]) Type.BYTE.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final byte dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        float[] src = (float[]) source;
                        byte[] dest = (byte[]) destination;
                        while ( length-- > 0 ) {
                            float sval = src[ srcPos ];
                            boolean isBad = sHandler.isBad( source, srcPos )
                                    || sval < MIN_BYTE
                                    || sval > MAX_BYTE;
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (byte) sval;
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        float[] src = (float[]) source;
                        byte[] dest = (byte[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_BYTE
                                        || dvald > (double) MAX_BYTE;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (byte) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.FLOAT && dType == Type.SHORT ) {
            short[] destbad = (short[]) Type.SHORT.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final short dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        float[] src = (float[]) source;
                        short[] dest = (short[]) destination;
                        while ( length-- > 0 ) {
                            float sval = src[ srcPos ];
                            boolean isBad = sHandler.isBad( source, srcPos )
                                    || sval < MIN_SHORT
                                    || sval > MAX_SHORT;
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (short) sval;
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        float[] src = (float[]) source;
                        short[] dest = (short[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_SHORT
                                        || dvald > (double) MAX_SHORT;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (short) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.FLOAT && dType == Type.INT ) {
            int[] destbad = (int[]) Type.INT.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final int dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        float[] src = (float[]) source;
                        int[] dest = (int[]) destination;
                        while ( length-- > 0 ) {
                            float sval = src[ srcPos ];
                            boolean isBad = sHandler.isBad( source, srcPos )
                                    || sval < MIN_INT
                                    || sval > MAX_INT;
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (int) sval;
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        float[] src = (float[]) source;
                        int[] dest = (int[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_INT
                                        || dvald > (double) MAX_INT;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (int) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.FLOAT && dType == Type.FLOAT ) {
            float[] destbad = (float[]) Type.FLOAT.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final float dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        float[] src = (float[]) source;
                        float[] dest = (float[]) destination;
                        while ( length-- > 0 ) {
                            boolean isBad = sHandler.isBad( source, srcPos );
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (float) src[ srcPos ];
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        float[] src = (float[]) source;
                        float[] dest = (float[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_FLOAT
                                        || dvald > (double) MAX_FLOAT;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (float) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.FLOAT && dType == Type.DOUBLE ) {
            double[] destbad = (double[]) Type.DOUBLE.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final double dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        float[] src = (float[]) source;
                        double[] dest = (double[]) destination;
                        while ( length-- > 0 ) {
                            boolean isBad = sHandler.isBad( source, srcPos );
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (double) src[ srcPos ];
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        float[] src = (float[]) source;
                        double[] dest = (double[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_DOUBLE
                                        || dvald > (double) MAX_DOUBLE;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (double) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.DOUBLE && dType == Type.DOUBLE &&
                sHandler.equals( dHandler ) &&
                mapper == null ) {
            return new ConvertWorker() {
                final boolean isUnit() {
                    return true;
                }
                final void convert( Object src, int srcPos,
                                    Object dest, int destPos, int length ) {
                    System.arraycopy( src, srcPos, dest, destPos, length );
                }
            };
        }
        else if ( sType == Type.DOUBLE && dType == Type.BYTE ) {
            byte[] destbad = (byte[]) Type.BYTE.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final byte dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        double[] src = (double[]) source;
                        byte[] dest = (byte[]) destination;
                        while ( length-- > 0 ) {
                            double sval = src[ srcPos ];
                            boolean isBad = sHandler.isBad( source, srcPos )
                                    || sval < MIN_BYTE
                                    || sval > MAX_BYTE;
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (byte) sval;
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        double[] src = (double[]) source;
                        byte[] dest = (byte[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_BYTE
                                        || dvald > (double) MAX_BYTE;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (byte) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.DOUBLE && dType == Type.SHORT ) {
            short[] destbad = (short[]) Type.SHORT.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final short dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        double[] src = (double[]) source;
                        short[] dest = (short[]) destination;
                        while ( length-- > 0 ) {
                            double sval = src[ srcPos ];
                            boolean isBad = sHandler.isBad( source, srcPos )
                                    || sval < MIN_SHORT
                                    || sval > MAX_SHORT;
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (short) sval;
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        double[] src = (double[]) source;
                        short[] dest = (short[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_SHORT
                                        || dvald > (double) MAX_SHORT;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (short) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.DOUBLE && dType == Type.INT ) {
            int[] destbad = (int[]) Type.INT.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final int dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        double[] src = (double[]) source;
                        int[] dest = (int[]) destination;
                        while ( length-- > 0 ) {
                            double sval = src[ srcPos ];
                            boolean isBad = sHandler.isBad( source, srcPos )
                                    || sval < MIN_INT
                                    || sval > MAX_INT;
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (int) sval;
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        double[] src = (double[]) source;
                        int[] dest = (int[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_INT
                                        || dvald > (double) MAX_INT;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (int) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.DOUBLE && dType == Type.FLOAT ) {
            float[] destbad = (float[]) Type.FLOAT.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final float dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        double[] src = (double[]) source;
                        float[] dest = (float[]) destination;
                        while ( length-- > 0 ) {
                            double sval = src[ srcPos ];
                            boolean isBad = sHandler.isBad( source, srcPos )
                                    || sval < MIN_FLOAT
                                    || sval > MAX_FLOAT;
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (float) sval;
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        double[] src = (double[]) source;
                        float[] dest = (float[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_FLOAT
                                        || dvald > (double) MAX_FLOAT;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (float) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else if ( sType == Type.DOUBLE && dType == Type.DOUBLE ) {
            double[] destbad = (double[]) Type.DOUBLE.newArray( 1 );
            dHandler.putBad( destbad, 0 );
            final double dbad = destbad[ 0 ];
            if ( mapper == null ) {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        double[] src = (double[]) source;
                        double[] dest = (double[]) destination;
                        while ( length-- > 0 ) {
                            boolean isBad = sHandler.isBad( source, srcPos );
                            dest[ destPos ] =
                                    isBad ? dbad
                                            : (double) src[ srcPos ];
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
            else {
                return new NonunitConvertWorker( dHandler ) {
                    final void convert( Object source, int srcPos,
                                        Object destination, int destPos,
                                        int length ) {
                        double[] src = (double[]) source;
                        double[] dest = (double[]) destination;
                        while ( length-- > 0 ) {
                            if ( sHandler.isBad( source, srcPos ) ) {
                                dest[ destPos ] = dbad;
                            }
                            else {
                                double dvald =
                                        mapper.func( (double) src[ srcPos ] );
                                boolean isBad = Double.isNaN( dvald )
                                        || dvald < (double) MIN_DOUBLE
                                        || dvald > (double) MAX_DOUBLE;
                                dest[ destPos ] =
                                        isBad ? dbad
                                                : (double) dvald;
                            }
                            destPos++;
                            srcPos++;
                        }
                    }
                };
            }
        }
        else {
            // assert false;
            return null;
        }
    }

    static interface Mapper {
        double func( double x );
    }
}
