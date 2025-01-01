package uk.ac.starlink.array;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderTest {

    @Test
    public void testOrder() {
        List orders = Order.allOrders();
        assertEquals( 2, orders.size() );
        assertTrue( orders.contains( Order.COLUMN_MAJOR ) );
        assertTrue( orders.contains( Order.ROW_MAJOR ) );
        assertTrue( Order.ROW_MAJOR != Order.COLUMN_MAJOR );
        assertTrue( Order.COLUMN_MAJOR.isFitsLike() );
        assertTrue( ! Order.ROW_MAJOR.isFitsLike() );
    }
}
