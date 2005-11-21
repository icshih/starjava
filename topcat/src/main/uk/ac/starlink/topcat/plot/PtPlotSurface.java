package uk.ac.starlink.topcat.plot;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.JComponent;
import uk.ac.starlink.table.ValueInfo;
import uk.ac.starlink.topcat.RowSubset;

/**
 * PlotSurface implementation which uses Ptplot classes for axis plotting
 * 
 * @author   Mark Taylor (Starlink)
 * @since    17 Jun 2004
 */
public class PtPlotSurface extends PlotBox implements PlotSurface {

    private PlotState state_;
    private PointSelection psel_;
    private SurfaceListener surfListener_;

    private static int PAD_PIXELS = 10;

    /**
     * Constructs a new surface, registering a listener which will be
     * notified any time the surface geometry changes (hence the markers
     * need to be redrawn).
     *
     * @param  surfListener listener
     */
    public PtPlotSurface( SurfaceListener surfListener ) {
        surfListener_ = surfListener;
        setColor( false );
        _setPadding( 0.0 );
        _expThreshold = 3;
    }

    public void setState( PlotState state ) {
        state_ = state;
        psel_ = state != null ? state.getPointSelection() : null;
        if ( state_ != null && state.getValid() ) {
            configure( state_ );
        }
        else {
            clearLegends();
            setXLabel( "" );
            setYLabel( "" );
            setXLog( false );
            setYLog( false );
            setXFlip( false );
            setYFlip( false );
            setGrid( false );
            checkInvariants();
        }
    }

    /*
     * The coordinates in a PlotBox confused me for some time.
     * What you have to remember is that PlotBox stores its state
     * (protected instance variables such as _xscale, _yscale, 
     * _xMax, _xMin, _yMax, _yMin etc) referring to the linear
     * coordinates which map onto the plotting surface.
     * So if you're using these variables, convert to the linear
     * space first (i.e. take logarithms if necessary).
     * This strikes me as a bit of a strange way to work, since it's
     * neither data coordinates (not logarithmic) but it's not graphics
     * coordinates either (double precision, has offset and scaling),
     * it's somewhere in between.
     */

    public void setDataRange( double xlo, double ylo, double xhi, double yhi ) {
        if ( ! Double.isNaN( xlo ) && ! Double.isNaN( xhi ) ) {
            if ( _xlog ) {
                xlo = xlo > 0.0 ? Math.log( xlo ) * _LOG10SCALE : 0.0;
                xhi = xhi > 0.0 ? Math.log( xhi ) * _LOG10SCALE : 1.0;
            }
            if ( _xflip ) {
                double xl = -xhi;
                double xh = -xlo;
                xlo = xl;
                xhi = xh;
            }
            int width = _lrx - _ulx;
            double xpad = ( xhi - xlo ) * PAD_PIXELS / width;
            setXRange( xlo - xpad, xhi + xpad );
        }
        if ( ! Double.isNaN( ylo ) && ! Double.isNaN( yhi ) ) {
            if ( _ylog ) {
                ylo = ylo > 0.0 ? Math.log( ylo ) * _LOG10SCALE : 0.0;
                yhi = yhi > 0.0 ? Math.log( yhi ) * _LOG10SCALE : 1.0;
            }
            if ( _yflip ) {
                double yl = -yhi;
                double yh = -ylo;
                ylo = yl;
                yhi = yh;
            }
            int height = _lry - _uly;
            double ypad = ( yhi - ylo ) * PAD_PIXELS / height;
            setYRange( ylo - ypad, yhi + ypad );
        }
        checkInvariants();
    }

//  public double[] getDataRange() {
//      return new double[] { _xMin, _yMin, _xMax, _yMax };
//  }

    public Point dataToGraphics( double dx, double dy, boolean insideOnly ) {
        if ( Double.isNaN( dx ) || Double.isInfinite( dx ) ||
             Double.isNaN( dy ) || Double.isInfinite( dy ) ) {
            return null;
        }
        if ( _xlog ) {
            if ( dx > 0.0 ) {
                dx = Math.log( dx ) * _LOG10SCALE;
            }
            else {
                return null;
            }
        }
        if ( _ylog ) {
            if ( dy > 0.0 ) {
                dy = Math.log( dy ) * _LOG10SCALE;
            }
            else {
                return null;
            }
        }
        if ( _xflip ) {
            dx = -dx;
        }
        if ( _yflip ) {
            dy = -dy;
        }
        if ( ! insideOnly || ( dx >= _xMin && dx <= _xMax &&
                               dy >= _yMin && dy <= _yMax ) ) {
            int px = _ulx + (int) ( ( dx - _xMin ) * _xscale );
            int py = _lry - (int) ( ( dy - _yMin ) * _yscale );
            return new Point( px, py );
        }
        else {
            return null;
        }
    }

    public double[] graphicsToData( int px, int py, boolean insideOnly ) {
        if ( insideOnly &&
             ( px < _ulx || px > _lrx || py < _uly || py > _lry ) ) {
            return null;
        }
        double dx = _xMin + ( ( px - _ulx ) / _xscale );
        double dy = _yMin - ( ( py - _lry ) / _yscale );
        if ( _xflip ) {
            dx = -dx;
        }
        if ( _yflip ) {
            dy = -dy;
        }
        if ( _xlog ) {
            dx = Math.pow( 10., dx );
        }
        if ( _ylog ) {
            dy = Math.pow( 10., dy );
        }
        return new double[] { dx, dy };
    }

    public Shape getClip() {
        int width = _lrx - _ulx;
        int height = _lry - _uly;
        return new Rectangle( _ulx, _uly, width, height );
    }

    public JComponent getComponent() {
        return this;
    }

    public void paintSurface( Graphics g ) {
        paintComponent( g );
    }

    protected void _zoom( int x, int y ) {
        double oldXMin = _xMin;
        double oldXMax = _xMax;
        double oldYMin = _yMin;
        double oldYMax = _yMax;
        super._zoom( x, y );
        if ( _xMin != oldXMin || _xMax != oldXMax ||
             _yMin != oldYMin || _yMax != oldYMax ) {
            checkInvariants();
            surfListener_.surfaceChanged();
        }
    }

    /**
     * Hack around the fact that PlotBox does a lot of its updating of
     * protected variables (which we use for coordinate conversion) in
     * its paintComponent method.  This is bad, because it means the
     * results we get are dependent on whether the redraw has actually
     * happened yet, which it might or might not have.
     * So here we effectively do a dry call of paintComponent, which
     * does the calculations and updates the state, without actually
     * writing any graphics.
     */
    private void checkInvariants() {
        _drawPlot( (Graphics) null, true );
    }

    /**
     * Ensures that the points are plotted in a consistent way.
     * Since data point plotting is taken care of by PlotSurface 
     * itself, this overridden method is in practice only used
     * by the legend-drawing routine.
     */
    protected void _drawPoint( Graphics g, int dataset,
                               long xpos, long ypos, boolean clip ) {
        psel_.getStyles()[ dataset ].drawLegend( g, (int) xpos, (int) ypos );
    }

    /**
     * Configures this plot box from a PlotState object.
     *
     * @param   state  state for configuration
     */
    private void configure( PlotState state ) {
        PointSelection psel = state.getPointSelection();

        /* Legend. */
        clearLegends();
        RowSubset[] rsets = psel.getSubsets();
        int nrset = rsets.length;
        for ( int iset = 0; iset < nrset; iset++ ) {
            addLegend( iset, rsets[ iset ].getName() );
        }

        /* Axes. */
        ValueInfo xInfo = state.getAxes()[ 0 ];
        ValueInfo yInfo = state.getAxes()[ 1 ];
        String xName = xInfo.getName();
        String yName = yInfo.getName();
        String xUnit = xInfo.getUnitString();
        String yUnit = yInfo.getUnitString();
        String xLabel = xName;
        String yLabel = yName;
        if ( xUnit != null && xUnit.trim().length() > 0 ) {
            xLabel = xLabel + " / " + xUnit;
        }
        if ( yUnit != null && yUnit.trim().length() > 0 ) {
            yLabel = yLabel + " / " + yUnit;
        }
        setXLabel( xLabel );
        setYLabel( yLabel );

        /* Logarithmic plot flags. */
        setXLog( state.getLogFlags()[ 0 ] );
        setYLog( state.getLogFlags()[ 1 ] );

        /* Axis flip flags. */
        setXFlip( state.getFlipFlags()[ 0 ] );
        setYFlip( state.getFlipFlags()[ 1 ] );

        /* Grid flag. */
        setGrid( state.getGrid() );

        checkInvariants();
    }
}
