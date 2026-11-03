package br.ufms.leiaufms.ui.common;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Aplica as barras do sistema e o teclado como padding (edge-to-edge do Android 15+). */
public final class InsetsHelper {

    private InsetsHelper() {
    }

    /** Ícones escuros nas barras do sistema no tema claro e claros no escuro. */
    public static void styleBars(android.app.Activity activity) {
        boolean night = (activity.getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        androidx.core.view.WindowInsetsControllerCompat controller =
                androidx.core.view.WindowCompat.getInsetsController(activity.getWindow(),
                        activity.getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(!night);
        controller.setAppearanceLightNavigationBars(!night);
    }

    public static void applySystemBars(View root) {
        applySystemBars(root, new View[0]);
    }

    /** Igual, e esconde as views dadas (barra de abas) enquanto o teclado está aberto. */
    public static void applySystemBars(View root, View... hideWithKeyboard) {
        final int left = root.getPaddingLeft();
        final int top = root.getPaddingTop();
        final int right = root.getPaddingRight();
        final int bottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(left + bars.left, top + bars.top, right + bars.right,
                    bottom + Math.max(bars.bottom, ime.bottom));
            boolean typing = windowInsets.isVisible(WindowInsetsCompat.Type.ime());
            for (View h : hideWithKeyboard) h.setVisibility(typing ? View.GONE : View.VISIBLE);
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
