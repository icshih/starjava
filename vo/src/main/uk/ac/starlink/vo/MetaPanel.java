package uk.ac.starlink.vo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLEditorKit;

/**
 * Panel for displaying metadata under headings.
 * It is designed to be contained in a scrollpane with vertical scrolling.
 *
 * @author   Mark Taylor
 * @since    16 Feb 2015
 */
public class MetaPanel extends JPanel implements Scrollable {

    private final JLabel logoLabel_;

    /**
     * Constructor.
     */
    public MetaPanel() {
        setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
        setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
        logoLabel_ = new JLabel();
        JComponent logoLine = Box.createHorizontalBox();
        logoLine.add( Box.createHorizontalGlue() );
        logoLine.add( logoLabel_ );
        add( logoLine );
    }

    /**
     * Adds a field for displaying a single-line item.
     *
     * @param   heading   item heading text
     * @return  component whose content can be set
     */
    public JTextComponent addLineField( String heading ) {
        JTextField field = new JTextField();
        field.setEditable( false );
        field.setOpaque( false );
        field.setBorder( BorderFactory.createEmptyBorder() );
        addHeadedComponent( heading, field );
        return field;
    }

    /**
     * Adds a field for displaying a text item with potentially multiple lines.
     *
     * @param   heading   item heading text
     * @return  component whose content can be set
     */
    public JTextComponent addMultiLineField( String heading ) {
        JTextArea field = new JTextArea();
        field.setLineWrap( true );
        field.setWrapStyleWord( true );
        field.setEditable( false );
        field.setOpaque( false );
        addHeadedComponent( heading, field );
        return field;
    }

    /**
     * Adds a field for displaying a text item formatted as HTML text.
     *
     * @param   heading   item heading text
     * @return  component whose content can be set
     */
    public JTextComponent addHtmlField( String heading ) {
        JEditorPane field = new JEditorPane() {
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();
                return new Dimension( size.width, Math.max( size.height, 15 ) );
            }
        };
        field.setEditorKit( new HTMLEditorKit() );
        field.putClientProperty( JEditorPane.HONOR_DISPLAY_PROPERTIES, true );
        field.setEditable( false );
        field.setOpaque( false );
        addHeadedComponent( heading, field );
        return field;
    }

    /**
     * Adds a field intended to contain a clickable URL.
     * If a non-null UrlHandler is supplied, its {@link UrlHandler#clickUrl}
     * method is invoked when the user clicks on this field.
     *
     * @param  heading  item heading text
     * @param  urlHandler  handler used when the field is clicked on;
     *                     may be null
     */
    public JTextComponent addUrlField( String heading,
                                       final UrlHandler urlHandler ) {
        final JTextField field = new JTextField() {
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        field.setEditable( false );
        field.setOpaque( false );
        field.setBorder( BorderFactory.createEmptyBorder() );
        final Action linkAct = new AbstractAction( null ) {
            public void actionPerformed( ActionEvent evt ) {
                String txt = field.getText();
                if ( txt != null && txt.length() > 0 && urlHandler != null ) {
                    try {
                        urlHandler.clickUrl( new URL( txt ) );
                    }
                    catch ( MalformedURLException e ) {
                    }
                }
            }
        };
        linkAct.putValue( Action.SHORT_DESCRIPTION,
                          "Open link in web browser" );
        JButton linkButton = new JButton( linkAct );
        linkButton.setBorder( BorderFactory.createEmptyBorder() );
        linkButton.setMargin( new java.awt.Insets( 0, 0, 0, 0 ) );
        JComponent line = Box.createHorizontalBox();
        line.add( field );
        line.add( Box.createHorizontalStrut( 5 ) );
        line.add( linkButton );
        line.add( Box.createHorizontalGlue() );
        addHeadedComponent( heading, line );
        if ( urlHandler != null ) {
            field.setForeground( new Color( 0x0000ee ) );
            field.addMouseListener( new MouseAdapter() {
                public void mouseClicked( MouseEvent evt ) {
                    linkAct.actionPerformed( null );
                }
            } );
            field.addCaretListener( new CaretListener() {
                public void caretUpdate( CaretEvent evt ) {
                    boolean hasUrl = false;
                    String txt = field.getText();
                    if ( txt != null && txt.length() > 0 ) {
                        try {
                            new URL( txt );
                            hasUrl = true;
                        }
                        catch ( MalformedURLException e ) {
                        }
                    }
                    linkAct.putValue( Action.SMALL_ICON,
                                      hasUrl ? ResourceIcon.EXTLINK : null );
                    linkAct.setEnabled( hasUrl );
                }
            } );
        }
        return field;
    }

    /**
     * Sets the content of a field.
     * As well as the obvious, it fixes it so that after the text is
     * added it's positioned correctly.
     *
     * @param  field  field
     * @param  text   new content
     */
    public void setFieldText( JTextComponent field, String text ) {

        /* Record the current position of this component in an ancestor
         * scroll pane, if any. */
        JScrollPane scroller =
            (JScrollPane)
            SwingUtilities.getAncestorOfClass( JScrollPane.class, this );
        final JScrollBar hbar = scroller == null
                              ? null
                              : scroller.getHorizontalScrollBar();
        final JScrollBar vbar = scroller == null
                              ? null
                              : scroller.getVerticalScrollBar();
        final int hpos = hbar == null ? -1 : hbar.getValue();
        final int vpos = vbar == null ? -1 : vbar.getValue();

        /* Set the text. */
        field.setText( text );

        /* Reset the caret to the start.  This means that long strings in
         * short one-line text fields are presented with the start,
         * not the end, of the text visible. */
        field.setCaretPosition( 0 );

        /* Restore the scrollbar state to whatever it was before the edit.
         * This isn't necessarily exactly where you'd like to see it,
         * but if you don't do this the thing scrolls all over the place.
         * The most common case is that the panel is scrolled to the top,
         * and you don't want it to scroll down as a consequence of editing
         * the content - this achieves that.
         * It has to be dispatched as a later event on the Event Dispatch
         * Thread because JTextComponent.setText, labelled thread-safe,
         * does not update the text in its own thread.  This EDT game is
         * not bulletproof, but seems to do the trick. */
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                if ( hbar != null ) {
                    hbar.setValue( hpos );
                }
                if ( vbar != null ) {
                    vbar.setValue( vpos );
                }
            }
        } );
    }

    /**
     * Sets an image to be displayed at the top of this panel.
     *
     * @param  logoIcon  image, may be null
     */
    public void setLogo( Icon logoIcon ) {
        logoLabel_.setIcon( logoIcon );
    }

    public Dimension getPreferredScrollableViewportSize() {
        return super.getPreferredSize();
    }

    public int getScrollableUnitIncrement( Rectangle visibleRect,
                                           int orientation, int direction ) {
        return getFontMetrics( getFont() ).getHeight();
    }

    public int getScrollableBlockIncrement( Rectangle visibleRect,
                                            int orientation, int direction ) {
        return getScrollableUnitIncrement( visibleRect, orientation,
                                           direction );
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    /**
     * Adds an item to this component with a heading.
     *
     * @param  heading   item heading text
     * @param  comp   item content component
     */
    private void addHeadedComponent( String heading, JComponent comp ) {
        JComponent headLine = Box.createHorizontalBox();
        headLine.add( new JLabel( heading + ":" ) );
        headLine.add( Box.createHorizontalGlue() );
        add( headLine );
        comp.setBorder( BorderFactory.createEmptyBorder( 0, 20, 0, 0 ) );
        add( comp );
    }
}