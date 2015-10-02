package uk.ac.starlink.ttools.plot2.layer;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import uk.ac.starlink.ttools.gui.ResourceIcon;
import uk.ac.starlink.ttools.func.Tilings;
import uk.ac.starlink.ttools.plot.Matrices;
import uk.ac.starlink.ttools.plot.Range;
import uk.ac.starlink.ttools.plot.Shader;
import uk.ac.starlink.ttools.plot.Shaders;
import uk.ac.starlink.ttools.plot.Style;
import uk.ac.starlink.ttools.plot2.AuxScale;
import uk.ac.starlink.ttools.plot2.DataGeom;
import uk.ac.starlink.ttools.plot2.Decal;
import uk.ac.starlink.ttools.plot2.Drawing;
import uk.ac.starlink.ttools.plot2.LayerOpt;
import uk.ac.starlink.ttools.plot2.PlotLayer;
import uk.ac.starlink.ttools.plot2.PlotUtil;
import uk.ac.starlink.ttools.plot2.Plotter;
import uk.ac.starlink.ttools.plot2.ReportKey;
import uk.ac.starlink.ttools.plot2.ReportMap;
import uk.ac.starlink.ttools.plot2.ReportMeta;
import uk.ac.starlink.ttools.plot2.Scaler;
import uk.ac.starlink.ttools.plot2.Scaling;
import uk.ac.starlink.ttools.plot2.Surface;
import uk.ac.starlink.ttools.plot2.config.ConfigKey;
import uk.ac.starlink.ttools.plot2.config.ConfigMap;
import uk.ac.starlink.ttools.plot2.config.ConfigMeta;
import uk.ac.starlink.ttools.plot2.config.IntegerConfigKey;
import uk.ac.starlink.ttools.plot2.config.OptionConfigKey;
import uk.ac.starlink.ttools.plot2.config.RampKeySet;
import uk.ac.starlink.ttools.plot2.config.StyleKeys;
import uk.ac.starlink.ttools.plot2.data.Coord;
import uk.ac.starlink.ttools.plot2.data.CoordGroup;
import uk.ac.starlink.ttools.plot2.data.DataSpec;
import uk.ac.starlink.ttools.plot2.data.DataStore;
import uk.ac.starlink.ttools.plot2.data.FloatingCoord;
import uk.ac.starlink.ttools.plot2.data.TupleSequence;
import uk.ac.starlink.ttools.plot2.geom.SkyDataGeom;
import uk.ac.starlink.ttools.plot2.geom.SkySurface;
import uk.ac.starlink.ttools.plot2.paper.Paper;
import uk.ac.starlink.ttools.plot2.paper.PaperType;

/**
 * Plotter that plots a genuine density map on a SkySurface.
 * It paints a single Decal, no Glyphs.
 *
 * <p>Note it only works with a SkySurface.
 *
 * @author   Mark Taylor
 * @since    20 Sep 2015
 */
public class SkyDensityPlotter
             implements Plotter<SkyDensityPlotter.SkyDenseStyle> {

    private final boolean transparent_;
    private final CoordGroup coordGrp_;
    private final FloatingCoord weightCoord_;

    /** Report key for the HEALPix level actually plotted. */
    public static final ReportKey<Integer> DISPLAYLEVEL_KEY =
        new ReportKey<Integer>( new ReportMeta( "level", "HEALPix Level" ),
                                Integer.class, false );

    private static FloatingCoord WEIGHT_COORD = FloatingCoord.WEIGHT_COORD;
    private static final RampKeySet RAMP_KEYS = StyleKeys.DENSEMAP_RAMP;
    private static final ConfigKey<Integer> LEVEL_KEY =
        IntegerConfigKey.createSpinnerPairKey(
            new ConfigMeta( "level", "HEALPix Level" )
           .setStringUsage( "<-rel-level|+abs-level>" )
           .setShortDescription( "HEALPix level, negative for relative" )
           .setXmlDescription( new String[] {
                "<p>Determines the HEALPix level of pixels which are averaged",
                "over to calculate density.",
                "</p>",
                "<p>If the supplied value is a non-negative integer,",
                "it gives the absolute level to use;",
                "at level 0 there are 12 pixels on the sky, and",
                "the count multiplies by 4 for each increment.",
                "</p>",
                "<p>If the value is negative, it represents a relative level;",
                "it is approximately the (negative) number of screen pixels",
                "along one side of a HEALPix sky pixel.",
                "In this case the actual HEALPix level will depend on",
                "the current zoom.",
                "</p>",
            } )
        , -3, 29, -8, "Abs", "Rel", DISPLAYLEVEL_KEY );
    private static final ConfigKey<Combiner> COMBINER_KEY = createCombinerKey();
    private static final ConfigKey<Double> OPAQUE_KEY = StyleKeys.AUX_OPAQUE;

    /**
     * Constructor.
     *
     * @param  transparent  if true, there will be a config option for
     *                      setting the alpha value of the whole layer
     * @param  hasWeight    if true, an optional weight coordinate will
     *                      be solicited alongside the positional coordinates
     */
    public SkyDensityPlotter( boolean transparent, boolean hasWeight ) {
        transparent_ = transparent;
        weightCoord_ = hasWeight ? FloatingCoord.WEIGHT_COORD : null;
        Coord[] extraCoords = weightCoord_ == null
                            ? new Coord[ 0 ]
                            : new Coord[] { weightCoord_ };
        coordGrp_ = CoordGroup.createCoordGroup( 1, extraCoords );
    }

    public String getPlotterName() {
        return "SkyDensity";
    }

    public Icon getPlotterIcon() {
        return ResourceIcon.FORM_SKYDENSITY;
    }

    public CoordGroup getCoordGroup() {
        return coordGrp_;
    }

    public boolean hasReports() {
        return false;
    }

    public String getPlotterDescription() {
        return PlotUtil.concatLines( new String[] {
            "<p>Plots a density map on the sky.",
            "</p>",
        } );
    }

    public ConfigKey[] getStyleKeys() {
        List<ConfigKey> keyList = new ArrayList<ConfigKey>();
        keyList.add( LEVEL_KEY );
        if ( weightCoord_ != null ) {
            keyList.add( COMBINER_KEY );
        }
        keyList.addAll( Arrays.asList( RAMP_KEYS.getKeys() ) );
        if ( transparent_ ) {
            keyList.add( OPAQUE_KEY );
        }
        return keyList.toArray( new ConfigKey[ 0 ] );
    }

    public SkyDenseStyle createStyle( ConfigMap config ) {
        RampKeySet.Ramp ramp = RAMP_KEYS.createValue( config );
        int level = config.get( LEVEL_KEY );
        Scaling scaling = ramp.getScaling();
        float scaleAlpha = (float) ( 1.0 / config.get( OPAQUE_KEY ) );
        Shader shader = Shaders.fade( ramp.getShader(), scaleAlpha );
        Combiner combiner = weightCoord_ == null ? Combiner.COUNT
                                                 : config.get( COMBINER_KEY );
        return new SkyDenseStyle( level, scaling, shader, combiner );
    }

    public PlotLayer createLayer( final DataGeom geom, final DataSpec dataSpec,
                                  final SkyDenseStyle style ) {
        LayerOpt opt = style.isOpaque() ? LayerOpt.OPAQUE : LayerOpt.NO_SPECIAL;
        return new AbstractPlotLayer( this, geom, dataSpec, style, opt ) {
            public Drawing createDrawing( Surface surface,
                                          Map<AuxScale,Range> auxRanges,
                                          PaperType paperType ) {
                return new SkyDensityDrawing( (SkySurface) surface,
                                              (SkyDataGeom) geom,
                                              dataSpec, style, paperType );
            }
        };
    }

    /**
     * Calculates the HEALPix level whose pixels are of approximately
     * the same size as the screen pixels for a given SkySurface.
     * There is not an exact correspondance here.
     * An attempt is made to return the result for the "largest" screen pixel
     * (the one covering more of the sky than any other).
     *
     * @param  surface
     * @return  approximately corresponding HEALPix level
     */
    private static int getPixelLevel( SkySurface surface ) {

        /* Identify the graphics pixel at the center of the sky projection.
         * It may be off the currently visible part of the screen;
         * that doesn't matter.  This is likely to be the largest
         * screen pixel. */
        Point p = surface.getSkyCenter();
        double[] p1 =
            surface.graphicsToData( new Point( p.x - 1, p.y - 1 ), null );
        double[] p2 =
            surface.graphicsToData( new Point( p.x + 1, p.y + 1 ), null );
        double pixTheta = vectorSeparation( p1, p2 ) / Math.sqrt( 4 + 4 );
        return Tilings.healpixK( Math.toDegrees( pixTheta ) );
    }

    /**
     * Angle in radians between two (not necessarily unit) vectors.
     * The code follows that of SLA_SEPV from SLALIB.
     * The straightforward thing to do would just be to use the cosine rule,
     * but that may suffer numeric instabilities for small angles,
     * so this more complicated approach is more robust.
     *
     * @param  p1  first input vector
     * @param  p2  second input vector
     * @return   angle between p1 and p2 in radians
     */
    private static double vectorSeparation( double[] p1, double[] p2 ) {
        double modCross = Matrices.mod( Matrices.cross( p1, p2 ) );
        double dot = Matrices.dot( p1, p2 );
        return modCross == 0 && dot == 0 ? 0 : Math.atan2( modCross, dot );
    }

    /**
     * Constructs the config key used to solicit a Combiner value
     * from the user.
     *
     * @return  combiner key
     */ 
    private static ConfigKey<Combiner> createCombinerKey() {
        ConfigMeta meta = new ConfigMeta( "combine", "Combine" );
        meta.setShortDescription( "Value combination mode" );
        meta.setXmlDescription( new String[] {
            "<p>Defines how values contributing to the same",
            "density map bin are combined together to produce",
            "the value assigned to that bin (and hence its colour).",
            "</p>",
            "<p>For unweighted values (a pure density map),",
            "it usually makes sense to use",
            "<code>" + Combiner.COUNT + "</code>.",
            "However, if the input is weighted by an additional",
            "data coordinate, one of the other values such as",
            "<code>" + Combiner.MEAN + "</code>",
            "may be more revealing.",
            "</p>", 
        } );
        Combiner[] options = Combiner.getKnownCombiners();
        Combiner dflt = Combiner.SUM;
        OptionConfigKey<Combiner> key =
                new OptionConfigKey<Combiner>( meta, Combiner.class,
                                               options, dflt ) {
            public String getXmlDescription( Combiner combiner ) {
                return combiner.getDescription();
            }
        };
        key.setOptionUsage();
        key.addOptionsXml();
        return key;
    }

    /**
     * Style for configuring with the sky density plot.
     */
    public static class SkyDenseStyle implements Style {

        private final int level_;
        private final Scaling scaling_;
        private final Shader shader_;
        private final Combiner combiner_;

        /**
         * Constructor.
         *
         * @param   level   HEALPix level defining the requested map resolution;
         *                  note the actual resolution at which the densities
         *                  are calculated may be different from this,
         *                  in particular if the screen pixel grid is coarser
         *                  than that defined by this level
         * @param   scaling   scaling function for mapping densities to
         *                    colour map entries
         * @param   shader   colour map
         * @param   combiner  value combination mode for bin calculation
         */
        public SkyDenseStyle( int level, Scaling scaling, Shader shader,
                              Combiner combiner ) {
            level_ = level;
            scaling_ = scaling;
            shader_ = shader;
            combiner_ = combiner;
        }

        /**
         * Indicates whether this style has any transparency.
         *
         * @return   if true, the colours painted by this shader within
         *           the plot's geometric region of validity (that is,
         *           on the sky) are guaranteed always to have an alpha
         *           value of 1
         */
        boolean isOpaque() {
            return ! Shaders.isTransparent( shader_ );
        }

        public Icon getLegendIcon() {
            return Shaders.createShaderIcon( shader_, null, true, 16, 8, 2, 2 );
        }

        @Override
        public int hashCode() {
            int code = 23443;
            code = 23 * code + level_;
            code = 23 * code + scaling_.hashCode();
            code = 23 * code + shader_.hashCode();
            code = 23 * code + combiner_.hashCode();
            return code;
        }

        @Override
        public boolean equals( Object o ) {
            if ( o instanceof SkyDenseStyle ) {
                SkyDenseStyle other = (SkyDenseStyle) o;
                return this.level_ == other.level_
                    && this.scaling_.equals( other.scaling_ )
                    && this.shader_.equals( other.shader_ )
                    && this.combiner_.equals( other.combiner_ );
            }
            else {
                return false;
            }
        }
    }

    /**
     * Drawing implementation for the sky density map.
     */
    private class SkyDensityDrawing implements Drawing {

        private final SkySurface surface_;
        private final SkyDataGeom geom_;
        private final DataSpec dataSpec_;
        private final SkyDenseStyle style_;
        private final PaperType paperType_;
        private final int level_;

        /**
         * Constructor.
         *
         * @param  surface  plotting surface
         * @param  geom    coordinate geometry
         * @param  dataSpec   specifies data coordinates
         * @param  style  density map style
         * @param  paperType  paper type
         */
        SkyDensityDrawing( SkySurface surface, SkyDataGeom geom,
                           DataSpec dataSpec, SkyDenseStyle style,
                           PaperType paperType ) {
            surface_ = surface;
            geom_ = geom;
            dataSpec_ = dataSpec;
            style_ = style;
            paperType_ = paperType;
            int pixLevel = getPixelLevel( surface );
            level_ = style_.level_ >= 0
                   ? Math.min( style_.level_, pixLevel )
                   : Math.max( 0, pixLevel + style_.level_ );
        }

        public Object calculatePlan( Object[] knownPlans,
                                     DataStore dataStore ) {
            for ( Object plan : knownPlans ) {
                if ( plan instanceof SkyDensityPlan ) {
                    SkyDensityPlan skyPlan = (SkyDensityPlan) plan;
                    if ( skyPlan.matches( level_, style_.combiner_,
                                          dataSpec_, geom_ ) ) {
                        return skyPlan;
                    }
                }
            }
            BinList binList = readBins( dataStore );
            return new SkyDensityPlan( level_, binList, dataSpec_, geom_ );
        }

        public void paintData( Object plan, Paper paper, DataStore dataStore ) {
            final SkyDensityPlan dplan = (SkyDensityPlan) plan;
            paperType_.placeDecal( paper, new Decal() {
                public void paintDecal( Graphics g ) {
                    paintBins( g, dplan.binList_ );
                }
                public boolean isOpaque() {
                    return style_.isOpaque();
                }
            } );
        }

        public ReportMap getReport( Object plan ) {
            ReportMap map = new ReportMap();
            if ( plan instanceof SkyDensityPlan ) {
                map.put( DISPLAYLEVEL_KEY,
                         new Integer( ((SkyDensityPlan) plan).level_ ) );
            }
            return map;
        }

        /**
         * Constructs and populates a bin list (weighted histogram) 
         * suitable for the plot from the data specified for this drawing.
         *
         * @param   dataStore   contains data required for plot
         * @return   populated bin list
         * @slow
         */
        private BinList readBins( DataStore dataStore ) {
            SkyPixer skyPixer = createSkyPixer();
            BinList binList = null;
            long npix = skyPixer.getPixelCount();
            Combiner combiner = style_.combiner_;
            if ( npix < 200000 ) {
                binList = combiner.createArrayBinList( (int) npix );
            }
            if ( binList == null ) {
                binList = new HashBinList( npix, combiner );
            }
            assert binList != null;
            int icPos = coordGrp_.getPosCoordIndex( 0, geom_ );
            int icWeight = weightCoord_ == null
                         ? -1
                         : coordGrp_.getExtraCoordIndex( 0, geom_ );
            TupleSequence tseq = dataStore.getTupleSequence( dataSpec_ );
            double[] v3 = new double[ 3 ];

            /* Unweighted. */
            if ( icWeight < 0 || dataSpec_.isCoordBlank( icWeight ) ) {
                while ( tseq.next() ) {
                    if ( geom_.readDataPos( tseq, icPos, v3 ) ) {
                        binList.submitToBin( skyPixer.getIndex( v3 ), 1 );
                    }
                }
            }

            /* Weighted. */
            else {
                while ( tseq.next() ) {
                    if ( geom_.readDataPos( tseq, icPos, v3 ) ) {
                        double w = weightCoord_
                                  .readDoubleCoord( tseq, icWeight );
                        if ( ! Double.isNaN( w ) ) {
                            binList.submitToBin( skyPixer.getIndex( v3 ), w );
                        }
                    }
                }
            }
            return binList;
        }

        /**
         * Given a prepared data structure, paints the results it represents
         * onto a graphics context appropriate for this layer drawing.
         *
         * @param  g  graphics context
         * @param  binList   histogram containing sky pixel values
         */
        private void paintBins( Graphics g, BinList binList ) {
            Rectangle bounds = surface_.getPlotBounds();

            /* Work out how to scale binlist values to turn into
             * entries in a colour map.  The first entry in the colour map
             * (index zero) corresponds to transparency. */
            Range densRange = new Range( binList.getBounds() );
            Scaler scaler =
                Scaling.createRangeScaler( style_.scaling_, densRange );
            IndexColorModel colorModel =
                PixelImage.createColorModel( style_.shader_, true );
            int ncolor = colorModel.getMapSize() - 1;

            /* Prepare a screen pixel grid. */
            int nx = bounds.width;
            int ny = bounds.height;
            Gridder gridder = new Gridder( nx, ny );
            int npix = gridder.getLength();
            int[] pixels = new int[ npix ];

            /* Iterate over screen pixel grid pulling samples from the
             * sky pixel grid for each screen pixel.  Note this is only
             * a good strategy if the screen oversamples the sky grid
             * (i.e. if the screen pixels are smaller than the sky pixels). */
            Point2D.Double point = new Point2D.Double();
            double x0 = bounds.x + 0.5;
            double y0 = bounds.y + 0.5;
            SkyPixer skyPixer = createSkyPixer();
            for ( int ip = 0; ip < npix; ip++ ) {
                point.x = x0 + gridder.getX( ip );
                point.y = y0 + gridder.getY( ip );
                double[] dpos = surface_.graphicsToData( point, null );

                /* Positions on the sky always have a value >= 1.
                 * Positions outside the sky coord range are untouched,
                 * so have a value of 0 (transparent). */
                if ( dpos != null ) {
                    double dval =
                        binList.getBinResult( skyPixer.getIndex( dpos ) );

                    /* NaN bin result corresponds to no submitted values;
                     * map it to zero here, which makes sense for many,
                     * though maybe not all, combiner types. */
                    if ( Double.isNaN( dval ) ) {
                        dval = 0;
                    }
                    pixels[ ip ] =
                        Math.min( 1 +
                                  (int) ( scaler.scaleValue( dval ) * ncolor ),
                                  ncolor - 1 );
                }
            }

            /* Copy the pixel grid to the graphics context using the
             * requested colour map. */
            new PixelImage( bounds.getSize(), pixels, colorModel )
               .paintPixels( g, bounds.getLocation() );
        }

        /**
         * Constructs an object which can map sky positions to a pixel
         * index in a HEALPix grid.
         *
         * @return   sky pixer for this drawing
         */
        private SkyPixer createSkyPixer() {
            return new SkyPixer( level_ );
        }
    }

    /**
     * Plot layer plan for the sky density map.
     * Note the basic data cached in the plan is currently the sky pixel
     * grid, not the screen pixel grid.  That means that drawing the
     * plot will take a little bit of time (though it will scale only
     * with plot pixel count, not with dataset size).
     */
    private static class SkyDensityPlan {
        final int level_;
        final BinList binList_;
        final DataSpec dataSpec_;
        final SkyDataGeom geom_;

        /**
         * Constructor.
         *
         * @param   level   HEALPix level
         * @param   binList  data structure containing sky pixel values
         * @param   dataSpec  data specification used to generate binList
         * @param   geom   sky geometry used to generate binList
         */
        SkyDensityPlan( int level, BinList binList, DataSpec dataSpec,
                        SkyDataGeom geom ) {
            level_ = level;
            binList_ = binList;
            dataSpec_ = dataSpec;
            geom_ = geom;
        }

        /**
         * Indicates whether this plan can be used for a given plot
         * specification.
         *
         * @param   level  HEALPix level giving sky pixel resolution
         * @param   combiner  value combination mode
         * @param   dataSpec  input data specification
         * @param   geom    sky geometry
         */
        public boolean matches( int level, Combiner combiner,
                                DataSpec dataSpec, SkyDataGeom geom ) {
             return level_ == level
                 && binList_.getCombiner().equals( combiner )
                 && dataSpec_.equals( dataSpec )
                 && geom_.equals( geom );
        }
    }
}