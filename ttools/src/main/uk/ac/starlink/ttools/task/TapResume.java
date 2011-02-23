package uk.ac.starlink.ttools.task;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.task.Environment;
import uk.ac.starlink.task.Parameter;
import uk.ac.starlink.task.TaskException;
import uk.ac.starlink.task.URLParameter;
import uk.ac.starlink.vo.TapQuery;
import uk.ac.starlink.vo.UwsJob;
import uk.ac.starlink.vo.UwsStage;

/**
 * Resumes an existing TAP query.
 *
 * @author   Mark Taylor
 * @since    23 Feb 2011
 */
public class TapResume extends ConsumerTask {

    private final URLParameter urlParam_;
    private final TapResultReader resultReader_;

    public TapResume() {
        super( "Resumes a previous query to a Table Access Protocol server",
               new ChoiceMode(), true );
        List<Parameter> paramList = new ArrayList<Parameter>();

        urlParam_ = new URLParameter( "joburl" );
        urlParam_.setPrompt( "Job URL for a previously created TAP query" );
        urlParam_.setDescription( new String[] {
            "<p>The URL of a job created by submission of a TAP query",
            "which was created earlier and has not yet been",
            "deleted (by the client) or destroyed (by the server).",
            "This will usually be of the form",
            "<code>&lt;tap-url&gt;/async/&lt;job-id&gt;</code>.",
            "You can also find out, and possibly retrieve results from",
            "the job by pointing a web browser at this URL.",
            "</p>",
        } );
        paramList.add( urlParam_ );

        resultReader_ = new TapResultReader();
        paramList.addAll( Arrays.asList( resultReader_.getParameters() ) );

        getParameterList().addAll( 0, paramList );
     
    }

    public TableProducer createProducer( Environment env )
            throws TaskException {
        final URL jobUrl = urlParam_.urlValue( env );
        final TapResultProducer resultProducer =
            resultReader_.createResultProducer( env );
        return new TableProducer() {
            public StarTable getTable() throws IOException {
                UwsJob uwsJob = new UwsJob( jobUrl );
                TapQuery query = new TapQuery( uwsJob );
                uwsJob.readPhase();
                UwsStage stage = UwsStage.forPhase( uwsJob.getLastPhase() );
                if ( stage == UwsStage.UNSTARTED ) {
                    uwsJob.start();
                }
                return resultProducer.waitForResult( query );
            }
        };
    }
}
