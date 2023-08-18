package uk.ac.starlink.topcat.plot2;

import java.util.Map;
import uk.ac.starlink.ttools.plot2.Ganger;
import uk.ac.starlink.ttools.plot2.LegendEntry;
import uk.ac.starlink.ttools.plot2.PlotLayer;
import uk.ac.starlink.ttools.plot2.Plotter;
import uk.ac.starlink.ttools.plot2.ReportMap;
import uk.ac.starlink.ttools.plot2.config.Specifier;
import uk.ac.starlink.topcat.TablesListComboBox;

/**
 * Control subinterface for controls that can contribute PlotLayers
 * to the plot.
 *
 * @author   Mark Taylor
 * @since    13 Mar 2013
 */
public interface LayerControl extends Control {

    /**
     * Returns a list of the plotters that will be used by this control
     * to create layers.
     *
     * @return   list of active plotters
     */
    Plotter<?>[] getPlotters();

    /**
     * Indicates whether this control will yield any layers in its
     * current state.
     * It returns true if {@link #getLayers getLayers}
     * will return a non-empty array.
     * False positives are best avoided, but permitted.
     *
     * @return  true if there is a non-zero number of layers
     */
    boolean hasLayers();

    /**
     * Returns the layers contributed by this control.
     *
     * @param   ganger  ganger within which layers will be used
     * @return  layers
     */
    TopcatLayer[] getLayers( Ganger<?,?> ganger );

    /**
     * Returns legend entries associated with this control.
     *
     * @return   legend entries
     */
    LegendEntry[] getLegendEntries();

    /** 
     * Accepts report information generated by plotting layers.
     * The submitted map may contain entries unrelated to this layer.
     * Null map values are permitted, with the same meaning as an empty map.
     *
     * @param   ganger  ganger within which layers will be used
     * @param   reports  per-layer plot reports for layers generated on
     *                   behalf of this and possibly other controls
     */
    void submitReports( Map<LayerId,ReportMap> reports, Ganger<?,?> ganger );

    /**
     * Returns a text label associated with one of the user coords for
     * this control, typically the name of the column or expression
     * supplying the data.  Null may be returned if there's no good answer.
     *
     * @param  userCoordName  name of one of the user value infos of an
     *                        input coord for this control
     * @return   text label associated with user coordinate
     */
    String getCoordLabel( String userCoordName );

    /**
     * Returns a specifier that determines which zone of a multi-zone plot
     * this control's layers will be displayed in.
     * If this control is known to be used in a single-zone context,
     * null should be returned.
     *
     * @return   zone id specifier, or null
     */
    Specifier<ZoneId> getZoneSpecifier();

    /**
     * Returns the selection widget for choosing which table this control
     * is working with.
     * If this control does not use a table, null is returned.
     * The assumption is (currently) that a given control uses a maximum
     * of one table.
     *
     * @return  table selector, or null
     */
    TablesListComboBox getTableSelector();
}
