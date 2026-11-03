package br.ufms.leiaufms.data.social;

/**
 * Quanto tempo um post fica no mural (pedido do Pedro, 03/11): 12 h, mais 12 h a cada 25 corações.
 * 0 corações = 12 h, 25 = 24 h, 50 = 36 h. A regra não é explicada na tela; a pessoa aprende com o uso.
 * A mesma conta está na consulta {@code SocialDao.feed}.
 */
public final class PostLifetime {

    public static final long BASE_HOURS = 12;
    public static final int HEARTS_PER_STEP = 25;
    public static final long HOUR_MS = 60L * 60 * 1000;

    private PostLifetime() {
    }

    public static long hours(int hearts) {
        return BASE_HOURS + BASE_HOURS * (Math.max(0, hearts) / HEARTS_PER_STEP);
    }

    public static boolean isVisible(long criadoEm, int hearts, long now) {
        return now <= criadoEm + hours(hearts) * HOUR_MS;
    }
}
