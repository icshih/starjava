package uk.ac.starlink.topcat.activate;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.TableColumnModel;
import uk.ac.starlink.table.ColumnData;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.gui.StarTableColumn;
import uk.ac.starlink.topcat.ColumnDataComboBoxModel;
import uk.ac.starlink.topcat.Outcome;
import uk.ac.starlink.topcat.TopcatModel;
import uk.ac.starlink.util.URLUtils;

/**
 * Partial ActivatorConfigurator implementation for activators
 * that view the location (URL) in a table column using some
 * viewer application.
 *
 * @author   Mark Taylor
 * @since    21 Dec 2017
 */
public abstract class UrlColumnConfigurator
                extends AbstractActivatorConfigurator {

    private final TopcatModel tcModel_;
    private final JComboBox colSelector_;
    private final Box queryPanel_;
    private static final String URLCOL_KEY = "url";

    /**
     * Constructor.
     *
     * @param   tinfo  topcat model information
     * @param   urlWord  descriptive term for what's in the URL
     *                   (displayed as part of GUI)
     * @param   urlFlags  list of flags whose columns are to be selected
     *                    as default values for the URL column selector;
     *                    earlier entries are preferred over later ones
     */
    protected UrlColumnConfigurator( TopcatModelInfo tinfo, String urlWord,
                                     ColFlag[] urlFlags ) {
        super( new JPanel( new BorderLayout() ) );
        JComponent panel = getPanel();
        tcModel_ = tinfo.getTopcatModel();
        queryPanel_ = Box.createVerticalBox();
        getPanel().add( queryPanel_, BorderLayout.NORTH );
        ColumnDataComboBoxModel colModel =
            new ColumnDataComboBoxModel( tcModel_, String.class, true );
        configureDefaultSelection( colModel, tinfo, urlFlags );
        colSelector_ = ColumnDataComboBoxModel.createComboBox();
        colSelector_.setModel( colModel );
        colSelector_.addActionListener( getActionForwarder() );
        JLabel colLabel = new JLabel( urlWord + " Location: " );
        JComponent colLine = Box.createHorizontalBox();
        colLine.add( colLabel );
        colLine.add( colSelector_ );
        queryPanel_.add( colLine );
        queryPanel_.add( Box.createVerticalStrut( 5 ) );
    }

    /**
     * Creates an activator from a ColumnData giving the URL string.
     *
     * @param   cdata  URL column data, will not be null
     * @return   new activator, or null
     */
    protected abstract Activator createActivator( ColumnData cdata );

    /**
     * Returns a config message given that a URL column has been supplied.
     *
     * @param   cdata  URL column data, will not be null
     * @return   message indicating why activator is not available, or null
     */
    protected abstract String getConfigMessage( ColumnData cdata );

    /**
     * Returns the component used for storing user interaction components.
     * This is a vertical box, which may have some components already in it.
     *
     * @return  query box
     */
    protected Box getQueryPanel() {
        return queryPanel_;
    }

    public Activator getActivator() {
        Object item = colSelector_.getSelectedItem();
        return item instanceof ColumnData
             ? createActivator( (ColumnData) item )
             : null;
    }

    public String getConfigMessage() {
        Object item = colSelector_.getSelectedItem();
        return item instanceof ColumnData
             ? getConfigMessage( (ColumnData) item )
             : "No location specified";
    }

    /**
     * Returns a partial config state, giving the current configuration
     * of the selected URL column.
     *
     * @return  url state
     */
    protected ConfigState getUrlState() {
        ConfigState state = new ConfigState();
        state.saveSelection( URLCOL_KEY, colSelector_ );
        return state;
    }

    /**
     * Restores the URL selection from a stored state object.
     *
     * @param  state  URL state
     */
    protected void setUrlState( ConfigState state ) {
        state.restoreSelection( URLCOL_KEY, colSelector_ );
    }

    /**
     * If a column exists with the named UCD, select it in the selector.
     *
     * @param  ucd  UCD to match
     */
    protected void selectColumnByUCD( String ucd ) {
        for ( int i = 0; i < colSelector_.getItemCount(); i++ ) {
            Object item = colSelector_.getItemAt( i );
            if ( item instanceof ColumnData ) {
                ColumnInfo cinfo = ((ColumnData) item).getColumnInfo();
                if ( ucd.equals( cinfo.getUCD() ) ) {
                    colSelector_.setSelectedIndex( i );
                    break;
                }
            }
        }
    }

    /**
     * If a column exists with the named UType, select it in the selector.
     *
     * @param  uType  UType to match
     */
    protected void selectColumnByUtype( String uType ) {
        String utype = uType.toLowerCase();
        for ( int i = 0; i < colSelector_.getItemCount(); i++ ) {
            Object item = colSelector_.getItemAt( i );
            if ( item instanceof ColumnData ) {
                ColumnInfo cinfo = ((ColumnData) item).getColumnInfo();
                String uT = cinfo.getUtype();
                String ut = uT == null ? null : uT.toLowerCase();
                if ( ut != null && ut.endsWith( utype ) ) {
                    colSelector_.setSelectedIndex( i );
                    if ( ut.equals( utype ) ||
                         ut.endsWith( ":" + utype ) ) {
                        break;
                    }
                }
            }
        };
    }

    /**
     * Returns a label which identifies a particular column in this
     * configurator's table.  Used for labelling display windows.
     *
     * @param   cdata  column data
     * @return   label
     */
    protected String getWindowLabel( ColumnData cdata ) {
        return cdata.getColumnInfo().getName()
             + "(" + tcModel_.getID() + ")";
    }

    /**
     * Tries to configure a column selector to choose columns that are marked
     * with given flags.
     *
     * @param   cdataModel   combo box model for choosing columns
     * @param   tinfo   topcat model info, including column flags
     * @param   urlFlags  list of flags whose columns are to be selected
     *                    as default values for the URL column selector;
     *                    earlier entries are preferred over later ones
     */
    private static void
            configureDefaultSelection( ColumnDataComboBoxModel cdataModel,
                                       TopcatModelInfo tinfo,
                                       ColFlag[] urlFlags ) {
        TopcatModel tcModel = tinfo.getTopcatModel();
        TableColumnModel tcolModel = tcModel.getColumnModel();
        int ntcol = tcolModel.getColumnCount();
        int ncdata = cdataModel.getSize();
        for ( ColFlag flag : urlFlags ) {
            for ( int itcol = 0; itcol < ntcol; itcol++ ) {
                if ( tinfo.columnHasFlag( itcol, flag ) ) {
                    String colName =
                        ((StarTableColumn) tcolModel.getColumn( itcol ))
                       .getColumnInfo().getName();
                    for ( int icd = 0; icd < ncdata; icd++ ) {
                        ColumnData cdata = cdataModel.getColumnDataAt( icd );
                        if ( cdata != null &&
                             cdata.toString().equals( colName ) ) {
                            cdataModel.setSelectedItem( cdata );
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Utility class providing a partial Activator implementation
     * for UrlColumnConfigurators that want a location (file or URL) value.
     */
    protected static abstract class LocationColumnActivator
            implements Activator {
        private final ColumnData cdata_;
        private final boolean invokeOnEdt_;

        /**
         * Constructor.
         *
         * @param  cdata  column data containing location strings
         * @param  invokeOnEdt  whether to invoke on the EDT
         */
        protected LocationColumnActivator( ColumnData cdata,
                                           boolean invokeOnEdt ) {
            cdata_ = cdata;
            invokeOnEdt_ = invokeOnEdt;
        }

        public Outcome activateRow( long lrow, ActivationMeta meta ) {
            Object value;
            try {
                value = cdata_.readValue( lrow );
            }
            catch ( IOException e ) {
                return Outcome.failure( e );
            }
            if ( value instanceof String ||
                 value instanceof URL ||
                 value instanceof URI ) {
                String loc = value.toString();
                if ( loc.trim().length() > 0 ) {
                    return activateLocation( loc );
                }
            }
            return Outcome.failure( value == null ? "No location"
                                                  : "Bad location: " + value );
        }

        public boolean invokeOnEdt() {
            return invokeOnEdt_;
        }

        /**
         * Consumes the location string corresponding to the row
         * to perform the activation action.
         *
         * @param  loc  location string, not null or blank
         * @return  outcome
         */
        protected abstract Outcome activateLocation( String loc );
    }

    /**
     * Utility class providing a partial Activator implementation
     * for UrlColumnConfigurators that want an actual URL value.
     */
    protected static abstract class UrlColumnActivator
            extends LocationColumnActivator {

        /**
         * Constructor.
         *
         * @param  cdata  column data containing location strings
         * @param  invokeOnEdt  whether to invoke on the EDT
         */
        protected UrlColumnActivator( ColumnData cdata, boolean invokeOnEdt ) {
            super( cdata, invokeOnEdt );
        }

        protected final Outcome activateLocation( String loc ) {
            final URL url;
            File file = new File( loc );
            if ( file.exists() ) {
                url = URLUtils.makeFileURL( file );
            }
            else {
                try {
                    url = new URL( loc );
                }
                catch ( MalformedURLException e ) {
                    return Outcome.failure( "Bad URL/no such file: " + loc );
                }
            }
            return activateUrl( url );
        }

        /**
         * Consumes the URL corresponding to the row
         * to perform the activation action.
         *
         * @param  url  URL, not null
         * @return  outcome
         */
        protected abstract Outcome activateUrl( URL url );
    }
}
