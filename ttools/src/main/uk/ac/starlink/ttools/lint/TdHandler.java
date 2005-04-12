package uk.ac.starlink.ttools.lint;

/**
 * Element handler for TD elements.
 *
 * @author   Mark Taylor (Starlink)
 */
public class TdHandler extends ElementHandler {

    private final StringBuffer content_ = new StringBuffer();

    public void characters( char[] ch, int start, int length ) {
        content_.append( ch, start, length );
    }

    public void endElement() {
        Ancestry family = getAncestry();
        DataHandler data = (DataHandler) 
                           family.getAncestor( DataHandler.class );
        if ( data != null ) {
            ValueParser parser = data.getField( family.getSiblingIndex() )
                                     .getParser();
            if ( parser != null ) {
                parser.checkString( content_.toString() );
            }
        }
        else {
            error( getName() + " outside DATA" );
        }
    }
}
