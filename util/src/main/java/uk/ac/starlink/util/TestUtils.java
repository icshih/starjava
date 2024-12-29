package uk.ac.starlink.util;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.Random;

/**
 * This class provides some additional methods, extracted from
 * the original TestCase class now situ in test/, for testing
 * in other modules.
 * <p>
 * Some of the methods are concerned with providing random values;
 * these are deterministic in that the random seed is set to a fixed
 * value when the test case is initialised, so a given test should
 * always be working with the same data, though the same call twice
 * in a given test will provide different random data.
 *
 * @author   Mark Taylor (Starlink)
 */
public class TestUtils {

    private static Random rand = new Random( 23L );

    /**
     * Fills a given array with random numbers between two floating point
     * values.
     * If the supplied minimum and maximum values are outside the 
     * range appropriate for the primitive type in question the range
     * will be suitably clipped.
     *
     * @param  array  an array of primitives to be filled with 
     *                random values
     * @param  min    the smallest value which will be used
     *                (will be converted to the appropriate primitive type)
     * @param  max    the largest value which will be used
     *                (will be converted to the appropriate primitive type)
     * @throws IllegalArgumentException  if <code>array</code> is not an array
     *         of a suitable primitive type
     */
    public static void fillRandom( Object array, double min, double max ) {
        Class<?> clazz = array.getClass().getComponentType();
        int size = Array.getLength( array );
        if ( clazz == byte.class ) {
            min = Math.max( min, (double) Byte.MIN_VALUE );
            max = Math.min( max, (double) Byte.MAX_VALUE );
            double range = max - min;
            byte[] arr = (byte[]) array;
            for ( int i = 0; i < size; i++ ) {
                arr[ i ] = (byte) ( min + rand.nextDouble() * range );
            }
        }
        else if ( clazz == short.class ) {
            min = Math.max( min, (double) Short.MIN_VALUE );
            max = Math.min( max, (double) Short.MAX_VALUE );
            double range = max - min;
            short[] arr = (short[]) array;
            for ( int i = 0; i < size; i++ ) {
                arr[ i ] = (short) ( min + rand.nextDouble() * range );
            }
        }
        else if ( clazz == int.class ) {
            min = Math.max( min, (double) Integer.MIN_VALUE );
            max = Math.min( max, (double) Integer.MAX_VALUE );
            double range = max - min;
            int[] arr = (int[]) array;
            for ( int i = 0; i < size; i++ ) {
                arr[ i ] = (int) ( min + rand.nextDouble() * range );
            }
        }
        else if ( clazz == long.class ) {
            min = Math.max( min, (double) Long.MIN_VALUE );
            max = Math.min( max, (double) Long.MAX_VALUE );
            double range = max - min;
            long[] arr = (long[]) array;
            for ( int i = 0; i < size; i++ ) {
                arr[ i ] = (long) ( min + rand.nextDouble() * range );
            }
        }
        else if ( clazz == float.class ) {
            min = Math.max( min, (double) -Float.MAX_VALUE );
            max = Math.min( max, (double) Float.MAX_VALUE );
            double range = max - min;
            float[] arr = (float[]) array;
            for ( int i = 0; i < size; i++ ) {
                arr[ i ] = (float) ( min + rand.nextDouble() * range );
            }
        }
        else if ( clazz == double.class ) {
            double range = max - min;
            double[] arr = (double[]) array;
            for ( int i = 0; i < size; i++ ) {
                arr[ i ] = min + rand.nextDouble() * range;
            }
        }
        else {
            throw new IllegalArgumentException( 
                "Unsupported array type or not an array " + 
                getClassName( array ) );
        }
    }

    /**
     * Fills a given array with random numbers between two integer values.
     * 
     * If the supplied minimum and maximum values are outside the 
     * range appropriate for the primitive type in question the range
     * will be suitably clipped.
     *
     * @param  array  an array of primitives to be filled with 
     *                random values
     * @param  min    the smallest value which will be used
     *                (will be converted to the appropriate primitive type)
     * @param  max    the largest value which will be used
     *                (will be converted to the appropriate primitive type)
     * @throws IllegalArgumentException  if <code>array</code> is not an array
     *         of a suitable primitive type
     */
    public static void fillRandom( Object array, int min, int max ) {
        fillRandom( array, (double) min, (double) max + 0.99 );
    }

    /**
     * Fills a given array with a regular pattern of integer values.
     * The elements of the array will take the values 
     * <code>min, min+1, min+2 .. max-1, min, min+1, min+2..</code> and so on.
     * If the <code>max&lt;min</code> then the values will start at
     * <code>min</code> and keep increasing.
     * <p>
     * The results might not be as expected if you use a <code>min</code> and
     * <code>max</code> values outside the range
     * of the numeric type in question.
     *
     * @param  array   an array of primitives to be filled with cycling values
     * @param  min     the first value
     * @param  max     the highest value, or if less than <code>min</code> an
     *                 indication that there is no maximum
     * @throws IllegalArgumentException  if <code>array</code> is not an array
     *         of a suitable primitive type
     */
    public static void fillCycle( Object array, int min, int max ) {
        Class<?> clazz = array.getClass().getComponentType();
        int size = Array.getLength( array );
        if ( clazz == byte.class ) {
            byte[] arr = (byte[]) array;
            byte val = (byte) min;
            for ( int i = 0; i < size; i++ ) {
                arr[ i ] = val++;
                if ( val > max ) val = (byte) min;
            }
        }
        else if ( clazz == short.class ) {
            short[] arr = (short[]) array;
            short val = (short) min;
            for ( int i = 0; i < size; i++ ) {
                arr[ i ] = val++;
                if ( val > max ) val = (short) min;
            }
        }
        else if ( clazz == int.class ) {
            int[] arr = (int[]) array;
            int val = min;
            for ( int i = 0; i < size; i++ ) {
                arr[ i ] = val++;
                if ( val > max ) val = min;
            }
        }
        else if ( clazz == long.class ) {
            long[] arr = (long[]) array;
            long val = min;
            for ( int i = 0; i < size; i++ ) {
                arr[ i ] = val++;
                if ( val > max ) val = min;
            }
        }
        else if ( clazz == float.class ) {
            float[] arr = (float[]) array;
            float val = min;
            for ( int i = 0; i < size; i++ ) {
                arr[ i ] = val++;
                if ( val > max ) val = min;
            }
        }
        else if ( clazz == double.class ) {
            double[] arr = (double[]) array;
            double val = min;
            for ( int i = 0; i < size; i++ ) {
                arr[ i ] = val++;
                if ( val > max ) val = min;
            }
        }
        else {
            throw new IllegalArgumentException(
                "Unsupported array type or not an array " + 
                getClassName( array ) );
        }
    }

    /**
     * Fills a given array with a pattern of values taken from another one.
     * <code>destArray</code> is filled up with copies of
     * <code>sourceArray</code>.
     * <code>destArray</code> and <code>sourceArray</code> must be arrays of the
     * same class (but can be different lengths of course).
     *
     * @param  destArray    array to be filled with items
     * @param  sourceArray  array containing source items
     */
    public static void fillCycle( Object destArray, Object sourceArray ) {
        Class<?> clazz = destArray.getClass();
        if ( ! clazz.isArray() || ! clazz.equals( sourceArray.getClass() ) ) {
            throw new IllegalArgumentException();
        }
        int nsrc = Array.getLength( sourceArray );
        int ndst = Array.getLength( destArray );
        for ( int start = 0; start < ndst; start += nsrc ) {
            System.arraycopy( sourceArray, 0, destArray, start,
                              Math.min( nsrc, ndst - start ) );
        }
    }

    /**
     * Tests whether or not a display, keyboard and mouse can in fact
     * be supported in this environment.   This differs from the
     * {@link GraphicsEnvironment#isHeadless} method in that
     * this one tries to do some graphics and if it catches a throwable
     * as a consequence it will return true.  The only time that
     * the <code>GraphicsEnvironment</code> call returns true in practice
     * is if you start java with the property
     * <code>java.awt.headless=true</code>.
     *
     * @return  <code>true</code> if graphics type stuff will fail
     */
    public static boolean isHeadless() {

        /* See if we know we're headless. */
        if ( GraphicsEnvironment.isHeadless() ) {
            return true;
        }

        /* Do something you can't do on a headless display. 
         * The code inside here may need some tweaking - seems to do 
         * the trick on linux & solaris.  Possibly it ought to check 
         * separately for presence of a mouse and keyboard as well? */
        try {
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        }
        catch ( Throwable e ) {
            return true;
        }
        return false;
    }

    /**
     * Returns the classname of an object.  Returns something sensible 
     * if <code>o</code> is null.
     *
     * @param  o  object
     * @return  name of <code>o</code>'s class, or "(null)"
     */
    private static String getClassName( Object o ) {
        return o == null ? "(null)" : o.getClass().getName();
    }

    private String combineMessages( String msg, String detail ) {
        return ( msg != null ) ? ( msg + " - " + detail )
                               : detail;
    }

    private String itemMismatchMessage( String msg, int ix ) {
        return combineMessages( msg, "element [" + ix + "] mismatch" );
    }

}
