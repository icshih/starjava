package uk.ac.starlink.votable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import uk.ac.starlink.table.ArrayColumn;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.ColumnStarTable;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StarTableFactory;
import uk.ac.starlink.table.StarTableOutput;
import uk.ac.starlink.table.StarTableWriter;
import uk.ac.starlink.table.StoragePolicy;
import uk.ac.starlink.table.Tables;
import uk.ac.starlink.util.DOMUtils;
import uk.ac.starlink.util.LogUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TableWriterTest {

    static final short IGNORE_ATTRIBUTE_PRESENCE = 1;
    static final short IGNORE_ATTRIBUTE_VALUE = 2;
    static final short IGNORE_WHITESPACE = 4;
    static final short IGNORE_COMMENTS = 8;

    @Test
    public void testTableWriter() throws Exception {
        checkTable( createTestTable( 23 ) );
    }

    @Test
    public void testTablesWriter() throws Exception {
        checkTables( new StarTable[] { createTestTable( 23 ) } );
        checkTables( new StarTable[] { createTestTable( 1 ),
                                       createTestTable( 10 ),
                                       createTestTable( 100 ) } );
        checkTables( new StarTable[ 0 ] );
    }

    @Test
    public void testOutputConfig() throws Exception {
        StarTable table = new StarTableFactory().makeStarTable( ":loop:10" );
        assertTrue( outputContains( table, "votable", "TABLEDATA" ) );
        assertFalse( outputContains( table, "votable", "BINARY" ) );
        assertTrue( outputContains( table, "votable(dataFormat=BINARY2)",
                                    "BINARY2" ) );
        assertFalse( outputContains( table, "votable", "VOTable/v1.1" ) );
        assertTrue( outputContains( table, "votable(votableVersion=V11)",
                                    "VOTable/v1.1" ) );
        assertTrue( outputContains( table,
                                    "votable(votableVersion=V14, "
                                          + "writeSchemaLocation=false, "
                                          + ",dataFormat=BINARY)",
                                    "<BINARY>" ) );
    }

    @Test
    public void testXmlConfigs() throws Exception {
        StarTable t1 = createTestTable( 1 );
        assertOutputStartsWith(
            t1, "votable",
            new byte[] { '<', '?', 'x', 'm', 'l', } );
        assertOutputStartsWith(
            t1, "votable(encoding=UTF-8)",
            new byte[] { '<', '?', 'x', 'm', 'l', } );
        assertOutputStartsWith(
            t1, "votable(encoding=UTF-16LE)",
            new byte[] { '<', 0, '?', 0, 'x', 0, 'm', 0, 'l', 0, } );

        StarTable t23 = createTestTable( 23 ); 
        assertDOMEquals(
            toDom( t23, "format=BINARY" ),
            toDom( t23, "format=BINARY,encoding=UTF-8" ) );
        assertDOMEquals(
            toDom( t23, "format=BINARY2" ),
            toDom( t23, "format=BINARY2,encoding=UTF-16" ) );
        assertDOMEquals(
            toDom( t23, "format=TABLEDATA" ),
            toDom( t23, "format=TABLEDATA,encoding=UTF-16LE" ) );

        // I don't think this test works.  Not sure about assertDOMEquals.
        assertDOMEquals(
            toDom( t23, "format=TABLEDATA,compact=false" ),
            toDom( t23, "format=TABLEDATA,compact=true" ) );
    }

    private VOElement toDom( StarTable table, String votOptions )
            throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StarTableOutput sto = new StarTableOutput();
        StarTableWriter handler =
            sto.getHandler( "votable(" + votOptions + ")");
        handler.writeStarTable( table, out );
        out.close();
        return new VOElementFactory( StoragePolicy.PREFER_MEMORY )
              .makeVOElement( new ByteArrayInputStream( out.toByteArray() ),
                              (String) null );
    }

    private boolean outputContains( StarTable table, String handlerName,
                                    String txt )
            throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StarTableOutput sto = new StarTableOutput();
        StarTableWriter handler = sto.getHandler( handlerName );
        handler.writeStarTable( table, out );
        String ser = new String( out.toByteArray(), "UTF-8" );
        return ser.indexOf( txt ) >= 0;
    }

    private void assertOutputStartsWith( StarTable table, String handlerName,
                                         byte[] testBuf )
            throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StarTableOutput sto = new StarTableOutput();
        StarTableWriter handler = sto.getHandler( handlerName );
        handler.writeStarTable( table, out );
        byte[] outBuf = new byte[ testBuf.length ];
        System.arraycopy( out.toByteArray(), 0, outBuf, 0, testBuf.length );
        assertArrayEquals( testBuf, outBuf );
    }

    public static void checkTable( StarTable table ) throws Exception {
        VOTableWriter[] writers = getAllTableWriters();
        for ( int i = 0; i < writers.length; i++ ) {
            VOTableWriter vowriter = writers[ i ];
            ByteArrayOutputStream ostrm = new ByteArrayOutputStream();
            vowriter.writeStarTable( table, ostrm );
            validate( vowriter.getVotableVersion(), ostrm.toByteArray() );
        }
    }

    public static void checkTables( StarTable[] tables ) throws Exception {
        VOTableWriter[] writers = getAllTableWriters();
        for ( int i = 0; i < writers.length; i++ ) {
            VOTableWriter vowriter = writers[ i ];
            ByteArrayOutputStream ostrm = new ByteArrayOutputStream();
            vowriter.writeStarTables( Tables.arrayTableSequence( tables ),
                                      ostrm );
            validate( vowriter.getVotableVersion(), ostrm.toByteArray() );
        }
    }

    private static void validate( VOTableVersion version, byte[] content )
            throws Exception {
        Schema schema = version.getSchema();
        if ( schema != null ) {
            schema.newValidator()
                  .validate( new SAXSource(
                                 new InputSource(
                                     new ByteArrayInputStream( content ) ) ) );
        }
    }

    private static StarTable createTestTable( int nrow ) {
        return AutoStarTable.getDemoTable( 100 );
    }

    public static VOTableWriter[] getAllTableWriters() {
        List<VOTableWriter> list = new ArrayList<VOTableWriter>();
        for ( VOTableVersion version :
              VOTableVersion.getKnownVersions().values() ) {
            for ( DataFormat format :
                  Arrays.asList( new DataFormat[] {
                      DataFormat.TABLEDATA,
                      DataFormat.BINARY,
                      DataFormat.FITS,
                      DataFormat.BINARY2,
                  } ) ) {
                if ( format != DataFormat.BINARY2 || version.allowBinary2() ) {
                    list.add( new VOTableWriter( format, true, version ) );
                }
            }
        }
        return list.toArray( new VOTableWriter[ 0 ] );
    }

    /**
     * Asserts that two DOMs are equal.
     *
     * @param expected the Node containing the expected DOM
     * @param actual the Node to be tested
     * @throws AssertionFailedError if the assertion is untrue
     * @see #assertDOMEquals(Node,Node,String,int)
     */
    public static void assertDOMEquals( Node expected, Node actual ) {
        assertDOMEquals( expected, actual, null, 0 );
    }

    /**
     * Asserts that two DOMs are equal.
     *
     * <p>If an assertion fails, the method indicates the location by
     * showing in the failure message the location of the mismatched
     * node, so that
     * <pre>
     * AssertionFailedError: .../test.xml:/[1]ndx/[2]data
     * expected: ...
     * </pre>
     * indicates that the assertion failed when examining the second child node
     * (which was a <code>&lt;data&gt;</code> element) of the first
     * child of the file <code>test.xml</code>
     *
     * <p>If the <code>flags</code> argument is non-zero, it indicates
     * a set of tests on the DOM to omit.  The value is ORed together
     * from the following constants:
     * <dl>
     * <dt><code>TestCase.IGNORE_ATTRIBUTE_PRESENCE</code>
     * <dt>do not check whether attributes match
     * <dt><code>TestCase.IGNORE_ATTRIBUTE_VALUE</code>
     * <dd>check that
     * the same attributes are present on the corresponding elements
     * in the tree, but do not check their values
     * <dt><code>TestCase.IGNORE_WHITESPACE</code>
     * <dd>skip whitespace-only text nodes
     * <dt><code>TestCase.IGNORE_COMMENTS</code>
     * <dd>skip comment nodes
     * </dl>
     *
     * @param expected the Node containing the expected DOM
     * @param actual the Node to be tested
     * @param context a string indicating the context, which will be
     * used in assertion failure reports.  May be null
     * @param flags a set of flags indicating which node tests to
     * omit.  Passing as zero includes all tests.
     * @throws AssertionFailedError if the assertion is untrue
     */
    public static void assertDOMEquals(Node expected,
                                       Node actual,
                                       String context,
                                       int flags) {
        if ( context == null )
            context = "TOP:";
        context = context + expected.getNodeName();
        assertNotNull( expected, context );
        assertNotNull( actual, context );
        if ( expected.getNodeType() != actual.getNodeType() ) {
            StringBuffer msg = new StringBuffer( context );
            msg.append( ": expected " )
                    .append( DOMUtils.mapNodeType( expected.getNodeType() ))
                    .append( "='" )
                    .append( expected.getNodeValue() )
                    .append( "', got " )
                    .append( DOMUtils.mapNodeType( actual.getNodeType() ))
                    .append( "='" )
                    .append( actual.getNodeValue() )
                    .append( "'" );
            fail( msg.toString() );
        }

        assertEquals(expected.getNodeType(), actual.getNodeType(), context+"(type)");

        /*
         * Comparing Nodes:
         *
         *   - Namespaces must be equal (or both null)
         *
         *   - If namespaces are null, then compare getNodeName
         *
         *     Don't compare localname in this case: getLocalName is
         *     documented to return null if the element was created
         *     with a DOM1 method such as Document.createElement
         *     (rather than createElementNS).  That is, getLocalName
         *     will return null or not depending on how the DOM was
         *     constructed, and so, if you compare the return values
         *     of getLocalName(), two equivalent DOMs could test
         *     unequal (because one getLocalName is null and the other
         *     isn't) if the two DOMs happened to be constructed by
         *     different routes.
         *
         *   - If namespaces are not null, then compare getLocalName
         *
         *     Don't compare prefixes, since these are defined to be arbitrary.
         *
         */
        String expectedNS = expected.getNamespaceURI();
        assertEquals(
                expectedNS,
                actual.getNamespaceURI(),
                context+"(ns)"
        );
        if ( expectedNS == null ) {
            assertEquals(
                    expected.getNodeName(),
                    actual.getNodeName(),
                    context+"(name)"
            );
        } else {
            assertEquals(
                    expected.getLocalName(),
                    actual.getLocalName(),
                    context+"(localName)"
            );
        }

        assertEquals(
                expected.getNodeValue(),
                actual.getNodeValue(),
                context+"(value)"
        );

        if ( (flags & IGNORE_ATTRIBUTE_PRESENCE) == 0 ) {
            NamedNodeMap okatts = expected.getAttributes();
            if ( okatts != null ) {
                NamedNodeMap testatts = actual.getAttributes();
                assertNotNull( testatts, context );
                assertEquals(
                        okatts.getLength(),
                        testatts.getLength(),
                        context+"(natts)"
                );
                for (int i=0; i<okatts.getLength(); i++) {
                    Attr okatt = (Attr)okatts.item(i);
                    Attr testatt = (Attr)testatts.getNamedItem( okatt.getName() );
                    assertNotNull( testatt );
                    if ( (flags & IGNORE_ATTRIBUTE_VALUE) == 0 )
                        assertEquals(
                                okatt.getValue(),
                                testatt.getValue(),
                                context+'@'+okatt.getName()
                        );
                }
            }
        }

        Node okkid = nextIncludedNode( expected.getFirstChild(), flags );
        Node testkid = nextIncludedNode( actual.getFirstChild(), flags );
        int kidno = 1;
        while ( okkid != null ) {
            assertNotNull( testkid, context+" too few kid elements" );
            assertDOMEquals
                    ( okkid,
                            testkid,
                            context + "/["+Integer.toString( kidno )+']',
                            flags );
            okkid = nextIncludedNode( okkid.getNextSibling(), flags );
            testkid = nextIncludedNode( testkid.getNextSibling(), flags );
            kidno++;
        }
        assertNull( testkid, context+" extra kids: "+testkid );
    }

    /**
     * Returns the first node from the set of this node and its
     * following siblings which is in the included set.
     *
     * @param n the node to follow
     * @param flags the set of nodes to include; if zero, the input
     *              node is returned unconditionally (ie, this method
     *              is a no-op)
     * @return the next interesting node, or null if
     *         there are none
     */
    private static Node nextIncludedNode(Node n, int flags) {
        if ( flags == 0 )
            return n;           // trivial case -- no omissions at all

        for ( /* no init */ ; n != null; n = n.getNextSibling() ) {
            boolean veto = false;

            switch ( n.getNodeType() ) {
                case Node.TEXT_NODE:
                    if ( (flags & IGNORE_WHITESPACE) != 0
                            && n.getNodeValue().trim().length() == 0 )
                        veto = true;
                    break;

                case Node.COMMENT_NODE:
                    if ( (flags & IGNORE_COMMENTS) != 0 )
                        veto = true;
                    break;

                default:
                    // do nothing -- this node is OK
            }
            if ( !veto )
                return n;       // JUMP OUT
        }
        assert n == null;

        return null;            // found nothing
    }
}
