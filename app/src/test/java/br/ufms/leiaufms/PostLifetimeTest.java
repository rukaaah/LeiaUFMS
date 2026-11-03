package br.ufms.leiaufms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import br.ufms.leiaufms.data.social.PostLifetime;

public class PostLifetimeTest {

    @Test
    public void degrausDe25Coracoes() {
        assertEquals(12, PostLifetime.hours(0));
        assertEquals(12, PostLifetime.hours(24));
        assertEquals(24, PostLifetime.hours(25));
        assertEquals(24, PostLifetime.hours(49));
        assertEquals(36, PostLifetime.hours(50));
    }

    @Test
    public void someDepoisDoPrazo() {
        long h = PostLifetime.HOUR_MS;
        assertTrue(PostLifetime.isVisible(0, 0, 12 * h));
        assertFalse(PostLifetime.isVisible(0, 0, 12 * h + 1));
        assertTrue(PostLifetime.isVisible(0, 25, 20 * h));
    }
}
