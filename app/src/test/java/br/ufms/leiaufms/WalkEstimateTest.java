package br.ufms.leiaufms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import br.ufms.leiaufms.core.location.WalkEstimate;

/** RF-28 / PC-08: até 2 km é "a pé"; tempo a 80 m/min. */
public class WalkEstimateTest {

    @Test
    public void walkableLimit() {
        assertTrue(WalkEstimate.isWalkable(0));
        assertTrue(WalkEstimate.isWalkable(2000));
        assertFalse(WalkEstimate.isWalkable(2001));
    }

    @Test
    public void minutes() {
        assertEquals(1, WalkEstimate.minutes(0));
        assertEquals(1, WalkEstimate.minutes(80));
        assertEquals(2, WalkEstimate.minutes(81));
        assertEquals(25, WalkEstimate.minutes(2000));
    }

    @Test
    public void distanceOfOneThousandthDegreeLatitude() {
        // 0,001° de latitude ≈ 111 m
        float d = WalkEstimate.distance(-20.0, -54.0, -20.001, -54.0);
        assertEquals(111.2f, d, 1.0f);
        assertEquals(0f, WalkEstimate.distance(-20.0, -54.0, -20.0, -54.0), 0.001f);
    }
}
