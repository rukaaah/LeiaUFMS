package br.ufms.leiaufms.ui.entry;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.ui.auth.AuthActivity;
import br.ufms.leiaufms.ui.main.MainActivity;

/**
 * RF-01: splash nativa (core-splashscreen) mantida só enquanto a sessão é verificada no Room.
 * Sem atraso artificial. Encaminha para Início (sessão válida) ou Login.
 */
public class EntryActivity extends AppCompatActivity {

    private volatile boolean sessionChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splash = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        splash.setKeepOnScreenCondition(() -> !sessionChecked);

        SessionManager session = SessionManager.get(this);
        AppExecutors.io().execute(() -> {
            SessionManager.Status status;
            try {
                status = session.validate();
            } catch (RuntimeException e) {
                status = SessionManager.Status.NONE;
            }
            final SessionManager.Status result = status;
            AppExecutors.main(() -> route(result));
        });
    }

    private void route(SessionManager.Status status) {
        Intent next;
        if (status == SessionManager.Status.VALID) {
            next = new Intent(this, MainActivity.class);
        } else {
            next = new Intent(this, AuthActivity.class);
            next.putExtra(AuthActivity.EXTRA_SESSION_EXPIRED, status == SessionManager.Status.EXPIRED);
        }
        next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        sessionChecked = true;
        startActivity(next);
        finish();
    }
}
