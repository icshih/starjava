package uk.ac.starlink.ndtools;

import uk.ac.starlink.array.BadHandler;
import uk.ac.starlink.array.BridgeNDArray;
import uk.ac.starlink.array.Converter;
import uk.ac.starlink.array.ConvertArrayImpl;
import uk.ac.starlink.array.Function;
import uk.ac.starlink.array.NDArray;
import uk.ac.starlink.array.Type;
import uk.ac.starlink.array.TypeConverter;
import uk.ac.starlink.ndx.DefaultMutableNdx;
import uk.ac.starlink.ndx.MutableNdx;
import uk.ac.starlink.ndx.Ndx;
import uk.ac.starlink.task.AbortException;
import uk.ac.starlink.task.DoubleParameter;
import uk.ac.starlink.task.Environment;
import uk.ac.starlink.task.Parameter;
import uk.ac.starlink.task.ParameterValueException;
import uk.ac.starlink.task.Task;

/**
 * Task for performing arithmetic on a constant and an NDX (e.g. multiplying
 * every pixel by a constant).
 */
class ConstArithmetic implements Task {

    private SumDoer iworker;
    private SumDoer vworker;

    private ExistingNdxParameter inpar;
    private NewNdxParameter outpar;
    private DoubleParameter constpar;

    public Parameter[] getParameters() {
        return new Parameter[] { inpar, outpar, constpar };
    }

    public String getUsage() {
        return "in out const";
    }

    /**
     * Constructs a ConstArithmetic object from objects which do the 
     * work on the image and variance components.
     *
     * @param  iworker  a SumDoer that handles image component image 
     *                  constant arithmetic
     * @param  vworker  a SumDoer that handles variance component image 
     *                  constant arithmetic
     */
    public ConstArithmetic( SumDoer iworker, SumDoer vworker ) {
        this.iworker = iworker;
        this.vworker = vworker;

        inpar = new ExistingNdxParameter( "in" );
        inpar.setPrompt( "Input NDX" );
        inpar.setPosition( 1 );

        outpar = new NewNdxParameter( "out" );
        outpar.setPrompt( "Output NDX" );
        outpar.setPosition( 2 );

        constpar = new DoubleParameter( "const" );
        constpar.setPrompt( "Constant value" );
        constpar.setPosition( 3 );
    }

    public void invoke( Environment env )
            throws ParameterValueException, AbortException {

        Ndx ndx1 = inpar.ndxValue();
        final double c = constpar.doubleValue();

        NDArray im;
        NDArray im1 = ndx1.getImage();
        if ( iworker == null ) {
            im = im1;
        }
        else {
            Type itype = im1.getType();
            BadHandler ibh = im1.getBadHandler();
            Function ifunc = new Function() {
                public double forward( double x ) {
                    return iworker.doSum( x, c );
                }
                public double inverse( double y ) {
                    throw new AssertionError();
                }
            };
            Converter iconv = 
                new TypeConverter( itype, ibh, itype, ibh, ifunc );
            im = new BridgeNDArray( new ConvertArrayImpl( im1, iconv ) );
        }
        NDArray var = null;
        if ( ndx1.hasVariance() ) {
            NDArray var1 = ndx1.getVariance();
            if ( vworker == null ) {
                var = var1;
            }
            else {
                Type vtype = var1.getType();
                BadHandler vbh = var1.getBadHandler();
                Function vfunc = new Function() {
                    public double forward( double x ) {
                        return vworker.doSum( x, c );
                    }
                    public double inverse( double y ) {
                        throw new AssertionError();
                    }
                };
                Converter vconv = 
                    new TypeConverter( vtype, vbh, vtype, vbh, vfunc );
                var = new BridgeNDArray( new ConvertArrayImpl( var1, vconv ) );
            }
        }

        NDArray qual = null;
        if ( ndx1.hasQuality() ) {
            qual = ndx1.getQuality();
        }

        MutableNdx ndx2 = new DefaultMutableNdx( im );
        ndx2.setVariance( var );
        ndx2.setVariance( qual );
        outpar.outputNdx( ndx2 );
    }
}
