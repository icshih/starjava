package uk.ac.starlink.table.gui;

import org.junit.jupiter.api.Test;

import javax.swing.Icon;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IconTest {

    @Test
    public void testIcons() {
        assert24( new LocationTableLoadDialog().getIcon() );
        assert24( new SQLTableLoadDialog().getIcon() );
        assert24( new FileChooserTableLoadDialog().getIcon() );
        assert24( new FilestoreTableLoadDialog().getIcon() );
        assert24( SystemBrowser.getSystemBrowserIcon() );
    }

    private void assert24( Icon icon ) {
        assertEquals( 24, icon.getIconWidth() );
        assertEquals( 24, icon.getIconHeight() );
    }
}
