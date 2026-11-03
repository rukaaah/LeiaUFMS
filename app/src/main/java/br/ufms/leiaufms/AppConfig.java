package br.ufms.leiaufms;

/**
 * Constantes de regra de negócio. Única fonte para prazos, raios e limites
 * (pendências PC-01 a PC-12 em docs/01-pre-documentacao.md).
 */
public final class AppConfig {

    private AppConfig() {
    }

    // RN-AUTH-01: domínio institucional
    public static final String ALLOWED_DOMAIN = "@ufms.br";

    // RN-AUTH-03
    public static final int PASSWORD_MIN = 8;
    public static final int PASSWORD_MAX = 64;
    // RN-AUTH-04
    public static final int NAME_MIN = 2;
    public static final int NAME_MAX = 80;
    public static final int SEMESTER_MIN = 1;
    public static final int SEMESTER_MAX = 12;
    public static final int STATUS_MAX = 80;
    public static final int EMAIL_MAX = 120;
    public static final int COURSE_MAX = 80;

    // ADR-04
    public static final int BCRYPT_COST = 12;
    public static final long SESSION_DURATION_MS = 30L * 24 * 60 * 60 * 1000;

    // PC-03 / PC-04
    public static final long REQUEST_EXPIRATION_MS = 72L * 60 * 60 * 1000;
    public static final long DELIVERY_DEADLINE_MS = 7L * 24 * 60 * 60 * 1000;
    public static final long DELIVERY_REMINDER_MS = 5L * 24 * 60 * 60 * 1000;

    // PC-06
    public static final int MAX_PENDING_REQUESTS = 5;

    // PC-08
    public static final float WALKING_MAX_METERS = 2000f;
    public static final float WALKING_METERS_PER_MINUTE = 80f;
    public static final int[] RADIUS_OPTIONS_METERS = {500, 1000, 2000, 5000};

    // ADR-03: só a câmera inicial do mapa (centro de Campo Grande, coordenada pública da cidade).
    // Não é ponto de encontro: pontos seguros são criados pelo Moderador no app (PC-07).
    public static final double MAP_START_LAT = -20.4428;
    public static final double MAP_START_LNG = -54.6464;
    public static final double MAP_START_ZOOM = 11.5;
    public static final String MAP_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty";

    // PC-09
    public static final int BADGE_DONOR_MIN = 3;
    public static final int BADGE_VETERAN_MIN = 10;

    // PC-10
    public static final long RATING_EDIT_WINDOW_MS = 48L * 60 * 60 * 1000;

    // PC-11
    public static final long FEED_WINDOW_MS = 14L * 24 * 60 * 60 * 1000;
    public static final int FEED_MAX_ITEMS = 30;
    public static final int POST_MAX_CHARS = 500;
    public static final int COMMENT_MAX_CHARS = 300;

    // PC-12
    public static final int CHAT_MAX_CHARS = 300;

    // RF-12
    public static final int TITLE_MAX = 150;
    public static final int AUTHOR_MAX = 120;
}
