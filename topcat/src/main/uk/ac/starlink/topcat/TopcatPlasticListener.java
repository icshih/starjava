package uk.ac.starlink.topcat;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.ComboBoxModel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import org.votech.plastic.PlasticHubListener;
import uk.ac.starlink.plastic.ApplicationItem;
import uk.ac.starlink.plastic.HubManager;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.Tables;
import uk.ac.starlink.util.DataSource;
import uk.ac.starlink.util.URLUtils;
import uk.ac.starlink.votable.DataFormat;
import uk.ac.starlink.votable.VOTableWriter;

/**
 * Implements the PlasticListener interface on behalf of the TOPCAT application.
 * Will attempt to unregister with the hub on finalization or JVM shutdown.
 *
 * @author   Mark Taylor
 * @since    8 Feb 2006
 * @see      <a href="http://plastic.sourceforge.net/">PLASTIC</a>
 */
public class TopcatPlasticListener extends HubManager {

    private final ControlWindow controlWindow_;
    private final Map idMap_;
    private final Map highlightMap_;

    public static final URI VOT_LOAD;
    public static final URI VOT_LOADURL;
    public static final URI VOT_SHOWOBJECTS;
    public static final URI VOT_HIGHLIGHTOBJECT;
    public static final URI INFO_GETICON;
    private static final URI[] SUPPORTED_MESSAGES = new URI[] {
        VOT_LOAD = createURI( "ivo://votech.org/votable/load" ),
        VOT_LOADURL = createURI( "ivo://votech.org/votable/loadFromURL" ),
        VOT_SHOWOBJECTS = createURI( "ivo://votech.org/votable/showObjects" ),
        VOT_HIGHLIGHTOBJECT =
            createURI( "ivo://votech.org/votable/highlightObject" ),
        INFO_GETICON = createURI( "ivo://votech.org/info/getIconURL" ),
    };
    public static final URI SKY_POINTAT =
        createURI( "ivo://votech.org/sky/pointAtCoords" );
 
    /**
     * Constructs a new listener which will react appropriately to 
     * messages from the hub.
     *
     * @param   controlWindow   control window into which accepted tables
     *          will be loaded etc
     */
    public TopcatPlasticListener( ControlWindow controlWindow ) {
        super( "topcat", SUPPORTED_MESSAGES );
        controlWindow_ = controlWindow;
        idMap_ = Collections.synchronizedMap( new HashMap() );
        highlightMap_ = new HashMap();
    }

    /**
     * Does the work for processing a hub message.
     *
     * @param  sender   sender ID
     * @param  message  message ID (determines the action required)
     * @param  args     message argument list
     * @return  return value requested by message
     */
    public Object doPerform( URI sender, URI message, List args )
            throws IOException {

        /* Load VOTable passed as text in an argument. */
        if ( VOT_LOAD.equals( message ) &&
             checkArgs( args, new Class[] { String.class } ) ) {
            String text = (String) args.get( 0 );
            String id = args.size() > 1 ? String.valueOf( args.get( 1 ) )
                                        : null;
            votableLoad( sender, text, id );
	    return Boolean.TRUE;
	}

        /* Load VOTable by URL. */
        else if ( VOT_LOADURL.equals( message ) &&
                  checkArgs( args, new Class[] { Object.class } ) ) {
            String url = args.get( 0 ) instanceof String
                       ? (String) args.get( 0 )
                       : args.get( 0 ).toString();
            votableLoadFromURL( sender, url );
            return Boolean.TRUE;
        }

        /* Select VOTable rows. */
        else if ( VOT_SHOWOBJECTS.equals( message ) && args.size() >= 2 &&
                  args.get( 1 ) instanceof List ) {
            String tableId = args.get( 0 ).toString();
            List objList = (List) args.get( 1 );
            return Boolean.valueOf( showObjects( sender, tableId, objList ) );
        }

        /* Highlight a single row. */
        else if ( VOT_HIGHLIGHTOBJECT.equals( message ) && args.size() >= 2 &&
                  args.get( 1 ) instanceof Number ) {
            String tableId = args.get( 0 ).toString();
            int irow = ((Number) args.get( 1 )).intValue();
            return Boolean.valueOf( highlightObject( sender, tableId, irow ) );
        }

        /* Get TOPCAT icon. */
        else if ( INFO_GETICON.equals( message ) ) {
            return "http://www.starlink.ac.uk/topcat/tc3.gif";
        }

        /* Unknown message. */
        else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns a ComboBoxModel which selects applications registered with
     * this hub manager; only those which support a given message are
     * included.  This listener is excluded.
     *
     * @param   messageId  message which must be supported
     * @return   selection model
     */
    public ComboBoxModel createPlasticComboBoxModel( URI messageId ) {
        return new SelectivePlasticListModel( getApplicationListModel(),
                                              messageId, true, this );
    }

    /**
     * Returns a new PlasticTransmitter which will transmit tables to
     * one or more listeners.
     *
     * @param  control  ControlWindow which supplies the currently selected
     *                  table ({@link ControlWindow#getCurrentModel})
     * @return  new table transmitter
     */
    public PlasticTransmitter createTableTransmitter( final 
                                                      ControlWindow control ) {
        return new PlasticTransmitter( this, VOT_LOADURL, "table" ) {
            protected void transmit( ApplicationItem app ) throws IOException {
                TopcatModel tcModel = control.getCurrentModel();
                if ( tcModel != null ) {
                    URI[] recipients = app == null
                                     ? null
                                     : new URI[] { app.getId() };
                    transmitTable( tcModel, recipients );
                }
            }
        };
    }

    /**
     * Sends a table to a specific list of PLASTIC listeners.
     *
     * @param  tcModel   the table model to broadcast
     * @param  recipients  listeners to receive it; null means do a broadcast
     */
    private void transmitTable( TopcatModel tcModel, final URI[] recipients )
            throws IOException {

        /* Get the hub and ID. */
        register();
        final PlasticHubListener hub = getHub();
        final URI plasticId = getRegisteredId();

        /* Write the data as a VOTable to a temporary file preparatory to
         * broadcast. */
        final File tmpfile = File.createTempFile( "plastic", ".vot" );
        final String tmpUrl = URLUtils.makeFileURL( tmpfile ).toString();
        tmpfile.deleteOnExit();
        OutputStream ostrm =
            new BufferedOutputStream( new FileOutputStream( tmpfile ) );
        try {
            new VOTableWriter( DataFormat.TABLEDATA, true )
               .writeStarTable( tcModel.getApparentStarTable(), ostrm );
        }
        catch ( IOException e ) {
            tmpfile.delete();
            throw e;
        }
        finally {
            ostrm.close();
        }

        /* Store a record of the table that was broadcast with its 
         * state. */
        int[] rowMap = tcModel.getViewModel().getRowMap();
        idMap_.put( tmpUrl, new TableWithRows( tcModel, rowMap ) );

        /* Do the broadcast, synchronously so that we don't delete the 
         * temporary file too early, but in another thread so that we
         * don't block the GUI. */
        new Thread( "PLASTIC table broadcast" ) {
            public void run() {
                List argList = Collections.singletonList( tmpUrl );
                Map responses = recipients == null 
                    ? hub.request( plasticId, VOT_LOADURL, argList )
                    : hub.requestToSubset( plasticId, VOT_LOADURL, argList,
                                           Arrays.asList( recipients ) );

                /* Delete the temp file. */
                tmpfile.delete();
            }
        }.start();
    }

    /**
     * Returns a new PlasticTransmitter which will transmit subsets to
     * one or more listeners.
     *
     * @param  subSelector  SubsetWindow which supplies the currently selected
     *         subset ({@link SubsetWindow#getSelectedSubset})
     */
    public PlasticTransmitter
           createSubsetTransmitter( final TopcatModel tcModel,
                                    final SubsetWindow subSelector ) {
        return new PlasticTransmitter( this, VOT_SHOWOBJECTS, "subset" ) {
            protected void transmit( ApplicationItem app ) throws IOException {
                RowSubset rset = subSelector.getSelectedSubset();
                if ( rset != null ) {
                    URI[] recipients = app == null
                                     ? null
                                     : new URI[] { app.getId() };
                    transmitSubset( tcModel, rset, recipients );
                }
            }
        };
    }

    /**
     * Sends a row subset to a specific list of PLASTIC listeners.
     * It uses the <code>ivo://votech.org/votable/showObjects</code> message.
     *
     * @param   tcModel  topcat model
     * @param   rset   row subset within tcModel
     * @param  recipients  listeners to receive it; null means do a broadcast
     */
    private void transmitSubset( TopcatModel tcModel, RowSubset rset, 
                                 final URI[] recipients )
            throws IOException {

        /* Get the hub and ID. */
        register();
        PlasticHubListener hub = getHub();
        URI plasticId = getRegisteredId();

        /* See if the table we're broadcasting the set for is any of the
         * tables we've previously broadcast.  If so, send the rows using
         * the same ID. */
        boolean done = false;
        for ( Iterator it = idMap_.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            String tableId = entry.getKey().toString();
            TableWithRows tr = (TableWithRows) entry.getValue();
            TopcatModel tcm = (TopcatModel) tr.tcModelRef_.get();
            if ( tcm != null && tcm == tcModel ) {
                List rowList = new ArrayList();

                /* Assemble a list of rows, possibly modulated by the
                 * row order when the table was sent originally. */
                int[] rowMap = tr.rowMap_;
                if ( rowMap == null ) {
                    int nrow =
                        (int) Math.min( (long) Integer.MAX_VALUE,
                                        tcModel.getDataModel().getRowCount() );
                    for ( int i = 0; i < nrow; i++ ) {
                        if ( rset.isIncluded( i ) ) {
                            rowList.add( new Integer( i ) );
                        }
                    }
                }
                else {
                    int nrow = rowMap.length;
                    for ( int i = 0; i < nrow; i++ ) {
                        if ( rset.isIncluded( rowMap[ i ] ) ) {
                            rowList.add( new Integer( i ) );
                        }
                    }
                }

                /* Send the request. */
                List argList =
                    Arrays.asList( new Object[] { tableId, rowList } );
                if ( recipients == null ) {
                    hub.requestAsynch( plasticId, VOT_SHOWOBJECTS, argList );
                }
                else {
                    hub.requestToSubsetAsynch( plasticId, VOT_SHOWOBJECTS,
                                               argList,
                                               Arrays.asList( recipients ) );
                }
                done = true;
            }
        }

        /* If that didn't result in any sends, try using the basic URL of
         * the table. */
        if ( ! done ) {
            URL url = URLUtils.fixURL( tcModel.getDataModel()
                                              .getBaseTable().getURL() );
            if ( url != null ) {
                List rowList = new ArrayList();
                int nrow =
                    (int) Math.min( (long) Integer.MAX_VALUE,
                                    tcModel.getDataModel().getRowCount() );
                for ( int i = 0; i < nrow; i++ ) {
                    if ( rset.isIncluded( i ) ) {
                        rowList.add( new Integer( i ) );
                    }
                }
                List argList =
                    Arrays.asList( new Object[] { url.toString(), rowList } );
                if ( recipients == null ) {
                    hub.requestAsynch( plasticId, VOT_SHOWOBJECTS, argList );
                }
                else {
                    hub.requestToSubsetAsynch( plasticId, VOT_SHOWOBJECTS,
                                               argList, 
                                               Arrays.asList( recipients ) );
                }
            }
        }
    }

    /** 
     * Transmits a request for listening applications to highlight a given
     * table row.
     *
     * @param   tcModel   topcat model of table to broadcast
     * @param   lrow      row index within tcModel
     * @param  array of plastic IDs for target applications;
     *         if null, broadcast will be to all
     * @return  true iff message was broadcast successfully
     */
    public boolean highlightRow( TopcatModel tcModel, long lrow, 
                                 URI[] recipients ) throws IOException {

        /* Get the hub and ID. */
        register();
        PlasticHubListener hub = getHub();
        URI plasticId = getRegisteredId();
        int irow = Tables.checkedLongToInt( lrow );

        /* See if the table we're broadcasting the row for is any of the
         * tables we've previously broadcast.  If so, send the row using the
         * same ID. */
        boolean done = false;
        int sendRow = -1;
        String tableId = null;
        for ( Iterator it = idMap_.entrySet().iterator();
              ! done && it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            TableWithRows tr = (TableWithRows) entry.getValue();
            TopcatModel tcm = (TopcatModel) tr.tcModelRef_.get();
            if ( tcm != null && tcm == tcModel ) {
                int[] rowMap = tr.rowMap_;
                if ( rowMap == null ) {
                    sendRow = irow;
                }
                else {
                    for ( int j = 0; j < rowMap.length; j++ ) {
                        if ( irow == rowMap[ j ] ) {
                            sendRow = j;
                            break;
                        }
                    }
                }
                tableId = entry.getKey().toString();
                done = true;
            }
        }

        /* If that didn't result in any sends, try using the basic URL of
         * the table. */
        if ( ! done ) {
            URL url = URLUtils.fixURL( tcModel.getDataModel()
                                              .getBaseTable().getURL() );
            if ( url != null ) {
                sendRow = irow;
                tableId = url.toString();
                done = true;
            }
        }

        /* Send the message if we've got the arguments. */
        if ( done && sendRow >= 0 ) {
            List args = Arrays.asList( new Object[] {
                tableId,
                new Integer( sendRow ),
            } );
            if ( recipients == null ) {
                hub.requestAsynch( plasticId, VOT_HIGHLIGHTOBJECT, args );
            }
            else {
                hub.requestToSubsetAsynch( plasticId, VOT_HIGHLIGHTOBJECT, args,
                                           Arrays.asList( recipients ) );
            }
            return true;
        }

        /* Otherwise return failure status. */
        else {
            return false;
        }
    }

    /**
     * Broadcasts a request for listening applications to point at a given
     * sky position.
     *
     * @param  ra2000  right ascension J2000.0 in degrees
     * @param  dec2000 declination J2000.0 in degrees
     * @param  array of plastic IDs for target applications;
     *         if null, broadcast will be to all
     */
    public void pointAt( double ra2000, double dec2000, URI[] recipients )
            throws IOException {
        register();
        PlasticHubListener hub = getHub();
        URI plasticId = getRegisteredId();
        List args = Arrays.asList( new Object[] { new Double( ra2000 ),
                                                  new Double( dec2000 ) } );
        if ( recipients == null ) {
            hub.requestAsynch( plasticId, SKY_POINTAT, args );
        }
        else {
            hub.requestToSubsetAsynch( plasticId, SKY_POINTAT, args,
                                       Arrays.asList( recipients ) );
        }
    }

    /**
     * Does the work for the load-from-string VOTable message.
     *
     * @param  sender  sender ID
     * @param  votText   VOTable text contained in a string, assumed UTF-8
     *                   encoded
     * @param  votId   identifies the sent VOTable for later use
     */
    private void votableLoad( URI sender, String votText, String votId )
            throws IOException {
        final byte[] votBytes;
        try {
            votBytes = votText.getBytes( "UTF-8" );
        }
        catch ( UnsupportedEncodingException e ) {
            throw (AssertionError)
                  new AssertionError( "JVMs are required to support UTF-8" )
                 .initCause( e );
        }
        votText = null;
        DataSource datsrc = new DataSource() {
            public InputStream getRawInputStream() {
                return new ByteArrayInputStream( votBytes );
            }
        };
        loadTable( controlWindow_.getTableFactory()
                  .makeStarTable( datsrc, "votable" ), sender, votId );
    }

    /**
     * Does the work for the load-from-URL VOTable load message.
     *
     * @param   sender  sender ID
     * @param   url  location of table
     */
    private void votableLoadFromURL( URI sender, String url )
            throws IOException {
        loadTable( controlWindow_.getTableFactory()
                  .makeStarTable( url, "votable" ), sender, url );
    }

    /**
     * Loads a StarTable into TOPCAT.
     *
     * @param  table  table to load
     * @param  sender  sender ID
     * @param  key   identifier for the loaded table
     */
    private void loadTable( final StarTable table, URI sender,
                            final String key ) {
        String name = table.getName();
        if ( name == null || name.trim().length() == 0 ) {
            name = sender.toString();
        }
        final String title = name;

        /* Best do it asynchronously since this may not be called from the
         * event dispatch thread. */
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                TopcatModel tcModel =
                    controlWindow_.addTable( table, title, true );
                if ( key != null && key.trim().length() > 0 ) {
                    idMap_.put( key, new TableWithRows( tcModel, null ) );
                }
            }
        } );
    }

    /**
     * Does the work for the highlight-object message.
     *
     * @param   sender  sender ID
     * @param   tableId  identifier for the table
     * @param   irow     row index corresponding to the table ID
     * @return  true  iff the highlight was successful
     */
    private boolean highlightObject( URI sender, String tableId, int irow ) {

        /* See if we have the table named in the message. */
        TableWithRows tr = (TableWithRows) lookupTable( tableId );
        final TopcatModel tcModel = tr == null
                                  ? null
                                  : (TopcatModel) tr.tcModelRef_.get();
        if ( tcModel != null ) {

            /* Find out what row is named in the message. */
            long lrow = tr.rowMap_ == null ? (long) irow
                                           : (long) tr.rowMap_[ irow ];

            /* Call a highlight on this row.  However, if it's the same
             * as the last-highlighted row for this table, do nothing.
             * The purpose of this is to avoid the possibility of
             * eternal PLASTIC ping-pong between two (or more) 
             * applications.  It doesn't completely work though. */
            Long lastHigh = (Long) highlightMap_.get( tcModel );
            if ( lastHigh == null || lastHigh.longValue() != lrow ) {
                tcModel.highlightRow( lrow );
            }
            highlightMap_.put( tcModel, new Long( lrow ) );
            return true;
        }
        return false;
    }

    /**
     * Does the work for the show-objects message.
     *
     * @param   sender  sender ID
     * @param   tableId  identifier for the table
     * @param   objList  list of row indices (should be Numbers)
     * @return   true iff highlight was successful
     */
    private boolean showObjects( URI sender, String tableId, List objList ) {
        TableWithRows tr = (TableWithRows) lookupTable( tableId );
        final TopcatModel tcModel = tr == null
                                  ? null
                                  : (TopcatModel) tr.tcModelRef_.get();
        if ( tcModel != null ) {

            /* Turn the list of row indices into a bit vector. */
            int[] rowMap = tr.rowMap_;
            BitSet mask = new BitSet();
            for ( Iterator it = objList.iterator(); it.hasNext(); ) {
                Object val = it.next();
                if ( val instanceof Number ) {
                    int index = ((Number) val).intValue();
                    mask.set( rowMap == null ? index : rowMap[ index ] );
                }
            }

            /* See if this is identical to an existing subset.  If so, don't
             * create a new one.  It's arguable whether this is the behaviour
             * that you want, but at least until we have some way to delete
             * subsets it's probably best to do it like this to cut down on
             * subset proliferation. */
            RowSubset matching = null;
            for ( Iterator it = tcModel.getSubsets().iterator();
                  matching == null && it.hasNext(); ) {
                RowSubset rset = (RowSubset) it.next();
                int nrow = Tables.checkedLongToInt( tcModel.getDataModel()
                                                           .getRowCount() );
                if ( matches( mask, rset, nrow ) ) {
                    matching = rset;
                }
            }

            /* If we've found an existing set with the same content, 
             * apply that one. */
            if ( matching != null ) {
                final RowSubset rset = matching;
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        tcModel.applySubset( rset );
                    }
                } );
            }

            /* Otherwise make sure we have a unique name for the new subset. */
            else {
                int ipset = 0;
                for ( Iterator it = tcModel.getSubsets().iterator();
                      it.hasNext(); ) {
                    String setName = ((RowSubset) it.next()).getName();
                    if ( setName.matches( "plastic-([0-9]+)" ) ) {
                        ipset = Math.max( ipset,
                                   Integer.parseInt( setName.substring( 8 ) ) );
                    }
                }
                String setName = "plastic-" + ( ipset + 1 );

                /* Then construct, add and apply the new subset. */
                final RowSubset rset = new BitsRowSubset( setName, mask );
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        tcModel.addSubset( rset );
                        tcModel.applySubset( rset );
                    }
                } );
            }
    
            /* Success return. */
            return true;
        }
        else {

            /* Failure return. */
            return false;
        }
    }

    /**
     * Attempts to locate a table by its ID.  This is currently a URL string;
     * either one got from a previous VOT_LOADURL message or one inherent
     * in the table.
     *
     * @param   tableId   table identifier URL string
     * @return  tableWithRows object corresponding to tableId, or null
     */
    private TableWithRows lookupTable( String tableId ) {
        TableWithRows tr = (TableWithRows) idMap_.get( tableId );
        if ( tr != null ) {
            return tr;
        }
        else {
            ListModel tablesList =
                ControlWindow.getInstance().getTablesListModel();
            for ( int i = 0; i < tablesList.getSize(); i++ ) {
                TopcatModel tcModel =
                    (TopcatModel) tablesList.getElementAt( i );
                URL url = tcModel.getDataModel().getBaseTable().getURL();
                if ( URLUtils.sameResource( url,
                                            URLUtils.makeURL( tableId ) ) ) {
                    return new TableWithRows( tcModel, null );
                }
            }
        }
        return null;
    }
    
    /**
     * Encapsulates a table plus its row order.
     */
    private static class TableWithRows {
        final Reference tcModelRef_;
        final int[] rowMap_;
        TableWithRows( TopcatModel tcModel, int[] rowMap ) {
            tcModelRef_ = new WeakReference( tcModel );
            rowMap_ = rowMap;
        }
    }

    /**
     * Determines whether a BitSet contains the same information as a
     * RowSubset.
     *
     * @param  mask  bit set
     * @param  rset  row subset
     * @param  nrow  number of rows over which they are required to match
     * @return  true iff they represent the same data
     */
    private static boolean matches( BitSet mask, RowSubset rset, int nrow ) {
        for ( int i = 0; i < nrow; i++ ) {
            if ( mask.get( i ) != rset.isIncluded( (long) i ) ) {
                return false;
            }
        }
        return true;
    }
}
