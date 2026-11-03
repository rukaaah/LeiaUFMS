package br.ufms.leiaufms.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.ui.common.InsetsHelper;
import br.ufms.leiaufms.ui.main.MainActivity;

/** Login e cadastro (RF-02, RF-03). Única tela acessível sem sessão, além da splash. */
public class AuthActivity extends AppCompatActivity {

    public static final String EXTRA_SESSION_EXPIRED = "session_expired";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        InsetsHelper.applySystemBars(findViewById(R.id.auth_container));
        InsetsHelper.styleBars(this);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.auth_container, new LoginFragment())
                    .commit();
            if (getIntent().getBooleanExtra(EXTRA_SESSION_EXPIRED, false)) {
                Snackbar.make(findViewById(R.id.auth_container), R.string.session_expired,
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

    void showRegister() {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.auth_container, new RegisterFragment())
                .addToBackStack("register")
                .commit();
    }

    void showLogin() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
    }

    void openMain() {
        long uid = br.ufms.leiaufms.core.security.SessionManager.get(this).currentUserId();
        Class<?> next = br.ufms.leiaufms.ui.onboarding.OnboardingState.isDone(this, uid)
                ? MainActivity.class : br.ufms.leiaufms.ui.onboarding.OnboardingActivity.class;
        Intent intent = new Intent(this, next);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
