package uk.ac.starlink.treeview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import uk.ac.starlink.ast.FrameSet;
import uk.ac.starlink.splat.data.SpecData;
import uk.ac.starlink.splat.data.SpecDataComp;
import uk.ac.starlink.splat.data.SpecDataImpl;
import uk.ac.starlink.splat.plot.DivaPlot;
import uk.ac.starlink.splat.util.SplatException;
import uk.ac.starlink.array.AccessMode;
import uk.ac.starlink.array.ArrayAccess;
import uk.ac.starlink.array.NDArray;
import uk.ac.starlink.array.NDShape;
import uk.ac.starlink.array.Requirements;
import uk.ac.starlink.array.Type;
import uk.ac.starlink.ndx.Ndx;
import uk.ac.starlink.ndx.Ndxs;

public class GraphViewer extends DivaPlot {

    public GraphViewer( Ndx ndx ) throws SplatException, IOException {
        super( new SpecDataComp( new NdxSpecDataImpl( ndx ) ) );
        SpecDataComp sdcomp = getSpecDataComp();
        SpecData sd = sdcomp.get( 0 );
        if ( ndx.hasVariance() ) {
            sd.setDrawErrorBars( true );
        }
        fitToWidth();
        fitToHeight();
        addAncestorListener( new AncestorListener() {
            public void ancestorAdded( AncestorEvent evt ) { configure(); }
            public void ancestorMoved( AncestorEvent evt ) { configure(); }
            public void ancestorRemoved( AncestorEvent evt ) {}
        } );
    }

    private void configure() {
        Component parent = getParent();
        int width = parent.getWidth();
        int height = parent.getHeight();
        fitToWidth();
        fitToHeight();
        setScale( 1, 1 );
        setPreferredSize( parent.getSize() );
    }


    /* Override all MouseListener methods, since we don't want these actions. */
    // These don't seem to do the trick.  Not sure where the DivaPlot's
    // mouse listening is getting done from then.
    //  /** No action. */
    //  public void mouseClicked( MouseEvent evt ) {}
    //  /** No action. */
    //  public void mouseEntered( MouseEvent evt ) {}
    //  /** No action. */
    //  public void mouseExited( MouseEvent evt ) {}
    //  /** No action. */
    //  public void mousePressed( MouseEvent evt ) {}
    //  /** No action. */
    //  public void mouseReleased( MouseEvent evt ) {}
    //  /** Do not register tracker. */
    //  public void addMouseMotionListener( MouseMotionListener mousey ) {
    //      if ( mousey.getClass().toString().indexOf( "DivaPlot" ) >= 0 ) {
    //          return;
    //      }
    //      else {
    //          super.addMouseMotionListener( mousey );
    //      }
    //  }

    

    private static class NdxSpecDataImpl extends SpecDataImpl {

        private Ndx ndx;
        private FrameSet ast;
        private double[] imData;
        private double[] errData;
        private NDShape shape;
        private String title;

        public NdxSpecDataImpl( Ndx ndx ) throws IOException {
            super( ndx.hasTitle() ? ndx.getTitle() : null );
            this.ndx = ndx;
            this.ast = Ndxs.getAst( ndx );
            this.shape = ndx.getImage().getShape();
            this.title = ndx.hasTitle() ? ndx.getTitle() : null;
            int npix = (int) ndx.getImage().getShape().getNumPixels();

            Requirements req = new Requirements( AccessMode.READ )
                              .setType( Type.DOUBLE )
                              .setWindow( shape )
                              .setBadValue( new Double( SpecData.BAD ) );
            NDArray image = Ndxs.getMaskedImage( ndx, req );
            imData = new double[ npix ];
            ArrayAccess imacc = image.getAccess();
            imacc.read( imData, 0, npix );
            imacc.close();

            if ( ndx.hasVariance() ) {
                NDArray errors = Ndxs.getMaskedErrors( ndx, req );
                errData = new double[ npix ];
                ArrayAccess erracc = errors.getAccess();
                erracc.read( errData, 0, npix );
                erracc.close();
            }

        }

        public FrameSet getAst() {
            return ast;
        }
        public double[] getData() {
            return imData;
        }
        public double[] getDataErrors() {
            return errData;
        }
        public int[] getDims() {
            return NDShape.longsToInts( shape.getDims() );
        }
        public String getFullName() {
            return null;
        }
        public String getShortName() {
            return title;
        }
        public String getDataFormat() {
            return "NDX";
        }
        public void save() throws SplatException {
            throw new SplatException( "Not built to save" );
        }
    }



}
