package br.ufms.leiaufms.data.pedido;

/**
 * RF-41: janela em que a localização pode ser compartilhada com a outra parte:
 * de 10 minutos antes até 30 minutos depois do horário do encontro.
 */
public final class MeetingWindow {

    public static final long BEFORE_MS = 10L * 60 * 1000;
    public static final long AFTER_MS = 30L * 60 * 1000;

    private MeetingWindow() {
    }

    public static boolean isOpen(long now, Long encontroEm) {
        return encontroEm != null && now >= encontroEm - BEFORE_MS && now <= encontroEm + AFTER_MS;
    }

    public static long opensAt(long encontroEm) {
        return encontroEm - BEFORE_MS;
    }
}
