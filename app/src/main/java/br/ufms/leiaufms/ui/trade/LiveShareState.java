package br.ufms.leiaufms.ui.trade;

import android.content.Context;

/**
 * Lembra se a pessoa ligou "Mostrar onde estou" para um encontro (RF-41), mesmo antes de a primeira
 * posição chegar. A chave inclui o horário do encontro: remarcar começa desligado de novo.
 */
final class LiveShareState {

    private static final String PREFS = "leia_localizacao_encontro";

    private LiveShareState() {
    }

    private static String key(long pedidoId, long userId, long encontroEm) {
        return "ligado_" + pedidoId + "_" + userId + "_" + encontroEm;
    }

    static boolean isOn(Context c, long pedidoId, long userId, long encontroEm) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(key(pedidoId, userId, encontroEm), false);
    }

    static void set(Context c, long pedidoId, long userId, long encontroEm, boolean on) {
        android.content.SharedPreferences.Editor e = c.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        if (on) e.putBoolean(key(pedidoId, userId, encontroEm), true);
        else e.remove(key(pedidoId, userId, encontroEm));
        e.apply();
    }
}
