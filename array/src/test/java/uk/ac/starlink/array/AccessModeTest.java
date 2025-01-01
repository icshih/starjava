package uk.ac.starlink.array;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccessModeTest {

    @Test
    public void testRead() {
        AccessMode read = AccessMode.READ;
        assertTrue( read.isReadable() );
        assertTrue( ! read.isWritable() );
    }

    @Test
    public void testWrite() {
        AccessMode write = AccessMode.WRITE;
        assertTrue( ! write.isReadable() );
        assertTrue( write.isWritable() );
    }

    @Test
    public void testUpdate() {
        AccessMode update = AccessMode.UPDATE;
        assertTrue( update.isReadable() );
        assertTrue( update.isWritable() );
    }
}
