package reso.testing;

import org.junit.Test;
import reso.common.Link;
import reso.common.Message;

import static org.junit.Assert.assertEquals;

public class TestDelays {

    public static final double LENGTH_M = 200;
    public static final int BIT_RATE = 10000;
    public static final int PKT_LENGTH = 125;

    private final Link<Message> link = new Link<Message>(LENGTH_M, BIT_RATE);

    @Test
    public void testPropagationDelay() {
        assertEquals(LENGTH_M / 200000000, link.getPropagationDelay(), 1e-8);
    }

    @Test
    public void testTransmissionDelay() {
        assertEquals(((double) PKT_LENGTH * 8) / BIT_RATE, link.getTransmissionDelay(PKT_LENGTH), 1e-8);
    }


}
