package uk.ac.starlink.topcat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.ListModel;

/**
 * Basic implementation of CheckBoxList.
 * This provides a simple internal implementation of the checkbox model
 * and uses a DefaultListModel for the data.
 * Various parts can be overridden as required.
 *
 * @author   Mark Taylor
 * @since    21 Dec 2017
 */
public class BasicCheckBoxList<T> extends CheckBoxList<T> {

    private final Set<T> activeSet_;

    /**
     * Constructor.
     *
     * @param   canSelect   true if list item selection is permitted
     */
    public BasicCheckBoxList( boolean canSelect ) {
        super( new DefaultListModel<T>(), canSelect, new JLabel() );
        activeSet_ = new HashSet<T>();
    }

    @Override
    public DefaultListModel<T> getModel() {
        @SuppressWarnings("unchecked")
        DefaultListModel<T> model = (DefaultListModel<T>) super.getModel();
        return model;
    }

    @Override
    public void setModel( ListModel<T> model ) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a list of all the items currently in this list.
     *
     * @return  list of all items
     */
    public List<T> getItems() {
        List<T> list = new ArrayList<T>();
        ListModel<T> listModel = getModel();
        for ( int i = 0; i < listModel.getSize(); i++ ) {
            list.add( listModel.getElementAt( i ) );
        }
        return list;
    }

    /**
     * Returns a list of the items currently in this list whose
     * check box is selected.
     *
     * @return   list of active items
     */
    public List<T> getCheckedItems() {
        List<T> list = new ArrayList<T>();
        for ( T item : getItems() ) {
            if ( isChecked( item ) ) {
                list.add( item );
            }
        }
        return list;
    }

    public boolean isChecked( T item ) {
        return activeSet_.contains( item );
    }

    public void setChecked( T item, boolean isChecked ) {
        if ( isChecked ) {
            activeSet_.add( item );
        }
        else {
            activeSet_.remove( item );
        }
    }

    public void moveItem( int ifrom, int ito ) {
        DefaultListModel<T> listModel = getModel();
        listModel.add( ito, listModel.remove( ifrom ) );
    }

    protected void configureEntryRenderer( JComponent entryRenderer, T item,
                                           int index ) {
        ((JLabel) entryRenderer).setText( toString( item ) );
    }

    /**
     * Maps list items to string values that can be displayed in the list.
     * This method is just called by {@link #configureEntryRenderer}.
     *
     * @param  item  list entry
     * @return  strinification of <code>item</code> for user display
     */
    protected String toString( T item ) {
        return item == null ? null : item.toString();
    }
}
