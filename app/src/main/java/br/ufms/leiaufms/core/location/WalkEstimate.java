package br.ufms.leiaufms.core.location;

import br.ufms.leiaufms.AppConfig;

/** RF-28: viabilidade a pé em linha reta (PC-08). Sem dependência do Android para testes JUnit. */
public final class WalkEstimate {

    private WalkEstimate() {
    }

    public static boolean isWalkable(float meters) {
        return meters >= 0 && meters <= AppConfig.WALKING_MAX_METERS;
    }

    /** Minutos a pé, arredondados para cima, mínimo 1. */
    public static int minutes(float meters) {
        if (meters <= 0) return 1;
        return Math.max(1, (int) Math.ceil(meters / AppConfig.WALKING_METERS_PER_MINUTE));
    }

    /** Distância em linha reta (haversine), em metros. Equivalente a Location.distanceBetween para curtas distâncias. */
    public static float distance(double lat1, double lng1, double lat2, double lng2) {
        double r = 6371008.8;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return (float) (2 * r * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }
}
