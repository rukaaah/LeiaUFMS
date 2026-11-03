package br.ufms.leiaufms.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.notification.Notifier;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.databinding.ActivityMainBinding;
import br.ufms.leiaufms.ui.auth.AuthActivity;
import br.ufms.leiaufms.ui.common.InsetsHelper;
import br.ufms.leiaufms.ui.entry.EntryActivity;

/**
 * Tela principal com 4 abas (ADR-02). Guarda de sessão (ADR-04): sem sessão validada volta ao
 * fluxo de entrada; ao retomar, revalida a sessão no Room.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        if (!SessionManager.get(this).hasValidatedSession()) {
            // Processo recriado ou acesso direto: a splash revalida a sessão
            restartFrom(EntryActivity.class);
            return;
        }
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsHelper.applySystemBars(binding.root);
        InsetsHelper.styleBars(this);

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host);
        navController = host.getNavController();
        AppBarConfiguration config = new AppBarConfiguration.Builder(
                R.id.homeFragment, R.id.feedFragment, R.id.tradesFragment, R.id.profileFragment).build();
        NavigationUI.setupWithNavController(binding.toolbar, navController, config);
        NavigationUI.setupWithNavController(binding.bottomNav, navController);
        // A seta de voltar passa pelo mesmo caminho do botão Voltar (respeita "descartar alterações?")
        binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        binding.toolbar.inflateMenu(R.menu.main_toolbar);
        MenuItem inbox = binding.toolbar.getMenu().findItem(R.id.action_inbox);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_inbox) {
                navController.navigate(R.id.inboxFragment);
                return true;
            }
            return false;
        });
        navController.addOnDestinationChangedListener((controller, destination, args) ->
                inbox.setVisible(destination.getId() != R.id.inboxFragment));
        long userId = SessionManager.get(this).currentUserId();
        AppDatabase.get(this).notificacaoDao().observeUnread(userId).observe(this, count -> {
            boolean unread = count != null && count > 0;
            inbox.setIcon(unread ? R.drawable.ic_notifications_dot : R.drawable.ic_notifications);
            MenuItemCompat.setContentDescription(inbox, unread
                    ? getString(R.string.title_inbox) + ". " + getString(R.string.inbox_new)
                    : getString(R.string.title_inbox));
        });
        if (savedInstanceState == null && getIntent().getBooleanExtra(Notifier.EXTRA_OPEN_INBOX, false)) {
            navController.navigate(R.id.inboxFragment);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding == null) return;
        SessionManager session = SessionManager.get(this);
        AppExecutors.io().execute(() -> {
            SessionManager.Status status = session.validate();
            if (status != SessionManager.Status.VALID) {
                AppExecutors.main(() -> {
                    Intent intent = new Intent(this, AuthActivity.class);
                    intent.putExtra(AuthActivity.EXTRA_SESSION_EXPIRED, status == SessionManager.Status.EXPIRED);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }

    /** RF-05: chamado depois da confirmação do logout. */
    public void logout() {
        SessionManager session = SessionManager.get(this);
        AppExecutors.io().execute(() -> {
            session.logout();
            AppExecutors.main(() -> restartFrom(AuthActivity.class));
        });
    }

    private void restartFrom(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
