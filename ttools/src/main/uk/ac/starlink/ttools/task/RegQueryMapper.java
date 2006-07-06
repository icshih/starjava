package uk.ac.starlink.ttools.task;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.task.Environment;
import uk.ac.starlink.task.ExecutionException;
import uk.ac.starlink.task.Parameter;
import uk.ac.starlink.task.ParameterValueException;
import uk.ac.starlink.task.TaskException;
import uk.ac.starlink.ttools.TableConsumer;
import uk.ac.starlink.vo.RegistryInterrogator;
import uk.ac.starlink.vo.RegistryQuery;
import uk.ac.starlink.vo.RegistryStarTable;

/**
 * Mapper that does the work for the RegQuery task.
 *
 * @author   Mark Taylor
 * @since    6 Jul 2006
 */
public class RegQueryMapper implements TableMapper {

    private final Parameter queryParam_;
    private final Parameter urlParam_;
    private final static String ALL_RECORDS = "ALL";

    /**
     * Constructor.
     */
    public RegQueryMapper() {
        queryParam_ = new Parameter( "query" );
        queryParam_.setPrompt( "Text of registry query" );
        queryParam_.setDescription( new String[] {
            "Text of an SQL WHERE clause defining which resource records",
            "you wish to retrieve from the registry.",
            "Some examples are:",
            "<ul>",
            "<li>serviceType='CONE'</li>",
            "<li>title like '%2MASS%'</li>",
            "<li>publisher like 'CDS%' and title like '%galax%'</li>",
            "</ul>",
            "The special value \"ALL\" will attempt to retrieve all the",
            "records in the registry",
            "(though this is not necessarily a sensible thing to do).",
        } );

        urlParam_ = new Parameter( "regurl" );
        urlParam_.setPrompt( "URL of registry service" );
        urlParam_.setDefault( RegistryInterrogator.DEFAULT_URL.toString() );
        urlParam_.setDescription( new String[] {
            "The URL of a SOAP endpoint which provides suitable",
            "registry query services.",
        } );
    }

    public Parameter[] getParameters() {
        return new Parameter[] {
            queryParam_,
            urlParam_,
        };
    }

    public int getInCount() {
        return 0;
    }

    public TableMapping createMapping( Environment env )
            throws TaskException {
        String queryText = queryParam_.stringValue( env );
        if ( ALL_RECORDS.toUpperCase()
            .equals( queryText.trim().toUpperCase() ) ) {
            queryText = null;
        }
        String urlText = urlParam_.stringValue( env );
        URL regURL;
        try {
            regURL = new URL( urlText );
        }
        catch ( MalformedURLException e ) {
            throw new ParameterValueException( urlParam_, "Bad URL: " + urlText,
                                               e );
        }
        final RegistryQuery query = new RegistryQuery( regURL, queryText );
        return new TableMapping() {
            public void mapTables( StarTable[] in, TableConsumer[] out )
                    throws TaskException, IOException {
                StarTable qt = null;
                try {
                    qt = new RegistryStarTable( query );
                }
                catch ( Exception e ) {
                    throw new ExecutionException( "Query failed: "
                                                + e.getMessage(), e );
                }
                out[ 0 ].consume( qt );
            }
        };
    }
}
