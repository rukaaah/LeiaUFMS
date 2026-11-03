package br.ufms.leiaufms.ui.onboarding;

import android.content.Context;

/** Guarda, por usuário, se o onboarding já foi visto (SharedPreferences, sem dado sensível). */
public final class OnboardingState {

    private static final String PREFS = "leia_onboarding";

    private OnboardingState() {
    }

    public static boolean isDone(Context c, long userId) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean("feito_" + userId, false);
    }

    public static void markDone(Context c, long userId) {
        c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean("feito_" + userId, true).apply();
    }
}
