package uk.ac.starlink.hdx;

import org.opentest4j.AssertionFailedError;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import uk.ac.starlink.util.DOMUtils;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DOMComparer {

    /** Flags for {@link #assertDOMEquals} */
    static final short IGNORE_ATTRIBUTE_PRESENCE = 1;
    static final short IGNORE_ATTRIBUTE_VALUE = 2;
    static final short IGNORE_WHITESPACE = 4;
    static final short IGNORE_COMMENTS = 8;
    static private javax.xml.parsers.DocumentBuilder docParser;

    // Extracted methods from test/uk/ac/starlink/util/TestCase for assertDOMEquals
    // TODO: As these methods are utilised by many packages, shall create a class for testing.
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

    /**
     * Asserts that a DOM is equivalent to the XML in a given URL.
     * Equivalent to <code>assertDOMEquals(dom, actual, filename, 0)</code> with
     * the first argument being the document element of the DOM read
     * from the URL, and the third argument being the last part
     * (the `file name') of the URL.
     *
     * @param url pointing to an XML file -- the document element of
     * this file is the expected value
     * @param actual the node which is being compared
     * @throws java.io.IOException if the file cannot be found
     * @throws org.xml.sax.SAXException if there is a problem parsing the XML
     * @throws javax.xml.parsers.ParserConfigurationException if the
     *            XML parser cannot be initialised
     * @throws AssertionFailedError if the assertion is untrue
     * @see #assertDOMEquals(Node,Node,String,int)
     */
    public static void assertDOMEquals(URL url, Node actual )
            throws java.io.IOException,
            org.xml.sax.SAXException,
            javax.xml.parsers.ParserConfigurationException {
        assertDOMEquals( url.openStream(),
                actual,
                url.toString().replaceFirst( ".*/", ".../" )+":", 0 );
    }

    /**
     * Asserts that a DOM is equivalent to the XML in a given URL.
     * Equivalent to <code>assertDOMEquals(dom, actual, context, flags)</code> with
     * the first argument being the document element of the DOM read
     * from the URL, and the third argument being the last part
     * (the `file name') of the URL.
     *
     * @param url     pointing to an XML file -- the document element of
     *                this file is the expected value
     * @param actual  the node which is being compared
     * @param context a string indicating the context of this; if
     *                <code>null</code>, it defaults to `string:'
     * @param flags   a set of flags controlling the comparison; see
     *                {@link #assertDOMEquals(Node,Node,String,int)}
     * @throws java.io.IOException if the file cannot be found
     * @throws org.xml.sax.SAXException if there is a problem parsing the XML
     * @throws javax.xml.parsers.ParserConfigurationException if the
     *            XML parser cannot be initialised
     * @throws AssertionFailedError if the assertion is untrue
     * @see #assertDOMEquals(Node,Node,String,int)
     */
    public static void assertDOMEquals( URL url,
                                        Node actual,
                                        String context,
                                        int flags )
            throws java.io.IOException,
            org.xml.sax.SAXException,
            javax.xml.parsers.ParserConfigurationException {
        assertDOMEquals( url.openStream(),
                actual,
                context,
                flags );
    }

    /**
     * Asserts that a DOM is equivalent to the DOM implied by the XML
     * in a given string.
     *
     * @see #assertDOMEquals(String,Node,String,int)
     */
    public static void assertDOMEquals( String s, Node n )
            throws
            java.io.IOException,
            org.xml.sax.SAXException,
            javax.xml.parsers.ParserConfigurationException {
        assertDOMEquals( s, n, "string:", 0 );
    }

    /**
     * Asserts that a DOM is equivalent to the DOM implied by the XML
     * in a given string.
     *
     * @param expected a string containing XML -- the document element of
     * this file is the expected value
     * @param actual the node which is compared
     * @param context a string indicating the context of this; if
     * <code>null</code>, it defaults to `string:'
     * @param flags a set of flags controlling the comparison; see
     * {@link #assertDOMEquals(Node,Node,String,int)}
     *
     * @throws AssertionFailedError if the assertion is untrue
     * @see #assertDOMEquals(Node,Node,String,int)
     */
    public static void assertDOMEquals( String expected,
                                        Node actual,
                                        String context,
                                        int flags )
            throws
            java.io.IOException,
            org.xml.sax.SAXException,
            javax.xml.parsers.ParserConfigurationException {
        java.io.ByteArrayInputStream bais
                = new java.io.ByteArrayInputStream( expected.getBytes() );
        assertDOMEquals( bais,
                actual,
                (context == null ? "string:" : context),
                flags );
    }

    /**
     * Asserts that a DOM is equivalent to the DOM read from a given stream.
     *
     *
     * @param s a stream from which XML may be read -- the document element of
     * the resulting DOM is the expected value
     * @param actual the node which is compared
     * @param context a string indicating the context of this; may be
     * <code>null</code>
     * @param flags a set of flags controlling the comparison; see
     * {@link #assertDOMEquals(Node,Node,String,int)}
     *
     * @throws AssertionFailedError if the assertion is untrue
     * @see #assertDOMEquals(Node,Node,String,int)
     */
    public static void assertDOMEquals( java.io.InputStream s, Node actual,
                                        String context, int flags )
            throws
            java.io.IOException,
            org.xml.sax.SAXException,
            javax.xml.parsers.ParserConfigurationException {
        if ( docParser == null ) {
            javax.xml.parsers.DocumentBuilderFactory factory
                    = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware( true );
            docParser = factory.newDocumentBuilder();
        }
        Document doc = docParser.parse( s );
        assertDOMEquals( doc.getDocumentElement(), actual, context, flags );
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
                        assertEquals( context+'@'+okatt.getName(),
                                okatt.getValue(), testatt.getValue() );
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
}
