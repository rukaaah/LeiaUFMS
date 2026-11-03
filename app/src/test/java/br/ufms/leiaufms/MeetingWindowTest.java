package br.ufms.leiaufms;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import br.ufms.leiaufms.data.pedido.MeetingWindow;

/** RF-41: localização só de 10 min antes até 30 min depois do encontro. */
public class MeetingWindowTest {

    private static final long MIN = 60_000L;
    private static final long ENCONTRO = 1_000_000_000L;

    @Test
    public void window() {
        assertFalse(MeetingWindow.isOpen(ENCONTRO - 11 * MIN, ENCONTRO));
        assertTrue(MeetingWindow.isOpen(ENCONTRO - 10 * MIN, ENCONTRO));
        assertTrue(MeetingWindow.isOpen(ENCONTRO, ENCONTRO));
        assertTrue(MeetingWindow.isOpen(ENCONTRO + 30 * MIN, ENCONTRO));
        assertFalse(MeetingWindow.isOpen(ENCONTRO + 31 * MIN, ENCONTRO));
        assertFalse(MeetingWindow.isOpen(ENCONTRO, null));
    }
}
