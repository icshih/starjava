package uk.ac.starlink.treeview;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import uk.ac.starlink.util.DataSource;

/**
 * A DataNode representing a zip archive stored in a file.
 *
 * @author   Mark Taylor (Starlink)
 */
public class ZipFileDataNode extends ZipArchiveDataNode {

    private ZipFile zfile;
    private File file;
    private List entries;

    /**
     * Initialises a <code>ZipFileDataNode</code> from a
     * <code>File</code> object.
     *
     * @param  file  a <code>File</code> object representing the file from
     *               which the node is to be created
     */
    public ZipFileDataNode( File file ) throws NoSuchDataException {
        super( file.getName(), getMagic( file ) );
        try {
            zfile = new ZipFile( file );
        }
        catch ( IOException e ) {
            throw new NoSuchDataException( e.getMessage() );
        }
        this.file = file;
        setLabel( file.getName() );
    }

    /**
     * Initialises a <code>ZipFileDataNode</code> from a <code>String</code>.
     *
     * @param  fileName  the absolute or relative name of the zip file.
     */
    public ZipFileDataNode( String fileName ) throws NoSuchDataException {
        this( new File( fileName ) );
    }

    public boolean hasParentObject() {
        return file.getAbsoluteFile().getParentFile() != null;
    }

    public Object getParentObject() {
        return file.getAbsoluteFile().getParentFile();
    }

    protected List getEntries() throws IOException {
        if ( entries == null ) {
            entries = new ArrayList();
            for ( Enumeration enEn = zfile.entries();
                  enEn.hasMoreElements(); ) {
                entries.add( enEn.nextElement() );
            }
        }
        return entries;
    }

    protected Iterator getChildIteratorAtLevel( String level, 
                                                final DataNode parent )
            throws IOException {
        final ZipArchiveDataNode zadn = this;
        final DataNodeFactory childMaker = getChildMaker();
        final int lleng = level.length();
        final String pathHead = getPath() + getPathSeparator() + level;

        /* Get an iterator over all the ZipEntries at the requested level. */
        final Iterator zentIt = getEntriesAtLevel( level ).iterator();

        /* Return an iterator which makes DataNodes from each ZipEntry. */
        return new Iterator() {
            public Object next() {

                /* Get the next ZipEntry at the requested level. */
                final ZipEntry zent = (ZipEntry) zentIt.next();
                final String subname = zent.getName().substring( lleng );

                /* If it is a directory, make a ZipBranchDataNode from it. */
                if ( zent.isDirectory() ) {
                    DataNode dnode = new ZipBranchDataNode( zadn, zent );
                    dnode.setCreator( new CreationState( parent ) );
                    dnode.setLabel( subname );
                    return dnode;
                }

                /* If it's a file, turn it into a DataSource pass it to
                 * the DataNodeFactory. */
                else {
                    DataSource datsrc = new PathedDataSource() {
                        public String getPath() {
                            return pathHead + subname;
                        }
                        protected long getRawLength() {
                            return zent.getSize();
                        }
                        protected InputStream getRawInputStream() 
                                throws IOException {
                            return zfile.getInputStream( zent );
                        }
                    };
                    datsrc.setName( subname );
                    try {
                        return childMaker.makeDataNode( parent, datsrc );
                    }
                    catch ( NoSuchDataException e ) {
                        return childMaker.makeErrorDataNode( parent, e );
                    }
                }
            }
            public boolean hasNext() {
                return zentIt.hasNext();
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
 
    }

    private static byte[] getMagic( File file ) throws NoSuchDataException {
        try {
            return startBytes( file, 8 );
        }
        catch ( IOException e ) {
            throw new NoSuchDataException( e );
        }
    }

}
