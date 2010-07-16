package uk.ac.starlink.datanode.tree;

import java.awt.Component;
import java.io.IOException;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import uk.ac.starlink.datanode.nodes.IconFactory;
import uk.ac.starlink.datanode.nodes.NodeUtil;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StarTableFactory;
import uk.ac.starlink.table.gui.LoadWorker;
import uk.ac.starlink.table.gui.TableConsumer;
import uk.ac.starlink.table.gui.TableLoadDialog;

/**
 * Table load dialogue based on a Treeview-like node chooser.
 *
 * @author   Mark Taylor (Starlink)
 * @since    1 Dec 2004
 */
public class TreeTableLoadDialog implements TableLoadDialog {
    private TableNodeChooser nodeChooser_;

    /**
     * Constructor. 
     */
    public TreeTableLoadDialog() {
    }

    public String getName() {
        return "Hierarchy Browser";
    }

    public String getDescription() {
        return "Load table using treeview-type browser";
    }

    public Icon getIcon() {
        return IconFactory.getIcon( IconFactory.HIERARCH );
    }

    public boolean isAvailable() {
        return true;
    }

    public boolean showLoadDialog( Component parent,
                                   final StarTableFactory factory,
                                   ComboBoxModel formatModel,
                                   TableConsumer eater ) {
        NodeUtil.setGUI( true );
        if ( nodeChooser_ == null ) {
            nodeChooser_ = createNodeChooser();
        }
        final StarTable table = nodeChooser_.chooseStarTable( parent );
        if ( table != null ) {
            String id = null;
            if ( id == null && table.getURL() != null ) {
                id = table.getURL().toString();
            }
            if ( id == null ) {
                id = table.getName();
            }
            if ( id == null ) {
                id = "Table";
            }
            if ( ! factory.requireRandom() || table.isRandom() ) {
                eater.loadStarted( id );
                return eater.loadSucceeded( table );
            }
            else {
                new LoadWorker( eater, id ) {
                    public StarTable[] attemptLoads() throws IOException {
                        return new StarTable[] { factory.randomTable( table ) };
                    }
                }.invoke();
                return true;
            }
        }
        else {
            return false;
        }
    }

    /**
     * Constructs a node chooser for use with this dialogue.
     *
     * @return   new node chooser
     */
    protected TableNodeChooser createNodeChooser() {
        return new TableNodeChooser();
    }
}
