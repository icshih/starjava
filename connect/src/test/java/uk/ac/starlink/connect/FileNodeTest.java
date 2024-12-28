package uk.ac.starlink.connect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FileNodeTest {

    @Test
    public void testList() throws IOException {
        File dir = new File( "." ).getCanonicalFile();
        FileBranch branch = new FileBranch( dir );
        assertEquals( branch.getName(), dir.getName() );
        assertEquals( -1, branch.getName().indexOf( '/' ) );
        assertEquals( branch.toString(), dir.getCanonicalPath() );
        Set<File> fchildren =
            new HashSet<File>( Arrays.asList( dir.listFiles() ) );
        for ( Iterator<File> it = fchildren.iterator(); it.hasNext(); ) {
            if ( it.next().isHidden() ) {
                it.remove();
            }
        }
        Set<Node> nchildren =
            new HashSet<Node>( Arrays.asList( branch.getChildren() ) );
        assertEquals( fchildren.size(), nchildren.size() );
        assertTrue(fchildren.size() > 0, "Please run in a non-empty directory" );
        for ( Iterator<File> it = fchildren.iterator(); it.hasNext(); ) {
            File f = it.next();
            Node n = FileNode.createNode( f );
            assertTrue( nchildren.remove( n ) );
        }
        assertTrue( nchildren.isEmpty() );
    }

    public void testIO() throws IOException {
        File dir = new File( "." );
        String dummyName = "FileTestNode.dummy-file";
        File dummyFile = new File( dummyName );
        dummyFile.deleteOnExit();
        assertTrue( ! dummyFile.exists() );
        assertTrue( dir.canWrite(), "Please run in writable directory" );
        Branch branch = (Branch) FileNode.createNode( dir );
        FileLeaf newLeaf = (FileLeaf) branch.createNode( dummyName );
        assertTrue( ! dummyFile.exists() );
        try {
            newLeaf.getDataSource().getIntro();
            fail();
        }
        catch ( FileNotFoundException e ) {
        }
        assertTrue( ! dummyFile.exists() );
        OutputStream ostrm = newLeaf.getOutputStream();
        ostrm.write( 0 );
        ostrm.close();
        assertTrue( dummyFile.exists() );
        assertArrayEquals( new byte[ 1 ], newLeaf.getDataSource().getIntro() );
        dummyFile.delete();
    }
}
