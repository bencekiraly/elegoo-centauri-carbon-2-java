package com.elegoo.cc2.discovery;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Test for CC2 printer discovery.
 */
public class CC2DiscoveryTest {
    
    @Test
    public void testDiscovery() {
        CC2Discovery discovery = new CC2Discovery();
        List<CC2DiscoveredPrinter> printers = discovery.discoverPrinters();
        
        // This test will find printers if available on the network
        assertNotNull(printers);
        assertTrue("Should return a list (empty if no printers found)", printers instanceof List);
        
        if (!printers.isEmpty()) {
            CC2DiscoveredPrinter printer = printers.get(0);
            assertNotNull("Printer should have a host name", printer.getHostName());
            assertNotNull("Printer should have a serial number", printer.getSerialNumber());
            assertNotNull("Printer should have an IP address", printer.getIpAddress());
            assertTrue("Printer should be in LAN mode", printer.isLanMode());
        }
    }
}
