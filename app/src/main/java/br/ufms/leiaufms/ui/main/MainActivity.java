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
        InsetsHelper.applySystemBars(binding.root, binding.bottomBar);
        InsetsHelper.styleBars(this);

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host);
        navController = host.getNavController();
        AppBarConfiguration config = new AppBarConfiguration.Builder(
                R.id.homeFragment, R.id.tradesFragment, R.id.feedFragment, R.id.wishesFragment, R.id.profileFragment).build();
        NavigationUI.setupWithNavController(binding.toolbar, navController, config);
        NavigationUI.setupWithNavController(binding.bottomNav, navController);
        setupBottomBar();
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
        observeMeeting(userId);
        observeTradeBadge(userId);
        observeWishBadge(userId);
        observeBanner();
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

    // ---- Barra inferior (feedback 03/11): Início em bola central, reselecionar volta ao topo ----

    private void setupBottomBar() {
        // Tocar de novo na aba em que você já está fecha as páginas abertas e volta ao começo dela
        binding.bottomNav.setOnItemReselectedListener(item -> navController.popBackStack(item.getItemId(), false));
        binding.homeBall.setOnClickListener(v -> {
            if (!navController.popBackStack(R.id.homeFragment, false)) {
                binding.bottomNav.setSelectedItemId(R.id.homeFragment);
            }
        });
        navController.addOnDestinationChangedListener((c, d, a) -> {
            // Páginas abertas com capa usam os botões próprios sobre a capa, sem barra de título
            int id = d.getId();
            boolean hero = id == R.id.listingDetailFragment || id == R.id.obraFragment;
            binding.toolbar.setVisibility(hero ? android.view.View.GONE : android.view.View.VISIBLE);
            boolean home = isInTab(d, R.id.homeFragment);
            binding.homeBall.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    getColor(home ? R.color.leia_primary : R.color.leia_primary_container)));
            binding.homeBall.setImageTintList(android.content.res.ColorStateList.valueOf(
                    getColor(home ? R.color.leia_on_primary : R.color.leia_on_primary_container)));
            // Indicador da aba ganha a cor do contexto: Mural com a cor viva do social
            boolean feed = d.getId() == R.id.feedFragment;
            binding.bottomNav.setItemActiveIndicatorColor(android.content.res.ColorStateList.valueOf(
                    getColor(feed ? R.color.post_dica_container : R.color.leia_primary_container)));
        });
    }

    private boolean isInTab(androidx.navigation.NavDestination d, int tabId) {
        for (androidx.navigation.NavBackStackEntry e : navController.getCurrentBackStack().getValue()) {
            if (e.getDestination().getId() == tabId) return true;
        }
        return d.getId() == tabId;
    }

    // ---- Faixa do próximo encontro (feedback 03/11): fixa acima das abas, em qualquer tela ----

    private static final long MIN = 60_000L;
    private final android.os.Handler meetingTick = new android.os.Handler(android.os.Looper.getMainLooper());
    private br.ufms.leiaufms.data.local.model.MeetingRow meeting;

    private void observeMeeting(long userId) {
        AppDatabase.get(this).pedidoDao().nextMeeting(userId, System.currentTimeMillis() - 30 * MIN)
                .observe(this, m -> {
                    meeting = m;
                    renderMeeting();
                });
        binding.meeting.meetingBar.setOnClickListener(v -> {
            if (meeting == null) return;
            Bundle args = new Bundle();
            args.putLong(br.ufms.leiaufms.ui.trade.TradeDetailFragment.ARG_ID, meeting.pedidoId);
            navController.navigate(R.id.tradeDetailFragment, args);
        });
    }

    private final Runnable meetingRefresh = this::renderMeeting;

    /** Número em âmbar na aba Trocas: quantos pedidos precisam da pessoa (nunca vermelho). */
    // ---- Aviso do app no nosso tema, com o app aberto (feedback 03/11) ----

    private final Runnable hideBanner = this::dismissBanner;

    private void observeBanner() {
        binding.banner.bannerClose.setOnClickListener(v -> dismissBanner());
        br.ufms.leiaufms.core.notification.InAppBanner.events().observe(this, e -> {
            if (e == null || e.shown) return;
            e.shown = true;
            boolean wish = br.ufms.leiaufms.core.notification.Notifier.CHANNEL_WISHES.equals(e.canal);
            boolean post = br.ufms.leiaufms.data.local.entity.Notificacao.DESTINO_POST.equals(e.destinoTipo);
            binding.banner.bannerIcon.setImageResource(wish ? R.drawable.ic_favorite
                    : post ? R.drawable.ic_comment : R.drawable.ic_swap);
            binding.banner.bannerIcon.setBackgroundResource(wish ? R.drawable.bg_circle_tertiary
                    : post ? R.drawable.bg_circle_success : R.drawable.bg_circle_primary);
            binding.banner.bannerIcon.setImageTintList(android.content.res.ColorStateList.valueOf(getColor(wish
                    ? R.color.leia_tertiary : post ? R.color.leia_success : R.color.leia_primary)));
            binding.banner.bannerTitle.setText(e.titulo);
            binding.banner.bannerText.setText(e.texto);
            binding.banner.bannerCard.setOnClickListener(v -> {
                dismissBanner();
                openDestination(e.destinoTipo, e.destinoId);
            });
            com.google.android.material.card.MaterialCardView card = binding.banner.bannerCard;
            card.removeCallbacks(hideBanner);
            card.setVisibility(android.view.View.VISIBLE);
            card.setTranslationY(-card.getHeight() - 200f);
            card.animate().translationY(0).setDuration(250).start();
            card.announceForAccessibility(e.titulo + ". " + e.texto);
            card.postDelayed(hideBanner, 6000);
        });
    }

    private void dismissBanner() {
        com.google.android.material.card.MaterialCardView card = binding.banner.bannerCard;
        card.removeCallbacks(hideBanner);
        if (card.getVisibility() != android.view.View.VISIBLE) return;
        card.animate().translationY(-card.getHeight() - 200f).setDuration(200)
                .withEndAction(() -> card.setVisibility(android.view.View.GONE)).start();
    }

    private void openDestination(String tipo, Long id) {
        if (tipo == null || id == null) {
            navController.navigate(R.id.inboxFragment);
            return;
        }
        android.os.Bundle args = new android.os.Bundle();
        switch (tipo) {
            case br.ufms.leiaufms.data.local.entity.Notificacao.DESTINO_PEDIDO:
                args.putLong(br.ufms.leiaufms.ui.trade.TradeDetailFragment.ARG_ID, id);
                navController.navigate(R.id.tradeDetailFragment, args);
                break;
            case br.ufms.leiaufms.data.local.entity.Notificacao.DESTINO_ANUNCIO:
                args.putLong(br.ufms.leiaufms.ui.listing.ListingDetailFragment.ARG_ID, id);
                navController.navigate(R.id.listingDetailFragment, args);
                break;
            case br.ufms.leiaufms.data.local.entity.Notificacao.DESTINO_POST:
                args.putLong(br.ufms.leiaufms.ui.feed.PostFragment.ARG_ID, id);
                navController.navigate(R.id.postFragment, args);
                break;
            default:
                args.putLong(br.ufms.leiaufms.ui.profile.ProfileFragment.ARG_USER_ID, id);
                navController.navigate(R.id.userProfileFragment, args);
        }
    }

    /** Número verde na aba Desejos: quantos desejos já dá para pedir. */
    private void observeWishBadge(long userId) {
        AppDatabase.get(this).socialDao().wishes(userId).observe(this, rows -> {
            int n = 0;
            if (rows != null) for (br.ufms.leiaufms.data.local.model.ShelfRow r : rows) if (r.disponiveis > 0) n++;
            com.google.android.material.badge.BadgeDrawable badge = binding.bottomNav.getOrCreateBadge(R.id.wishesFragment);
            badge.setBackgroundColor(getColor(R.color.leia_success));
            badge.setBadgeTextColor(getColor(R.color.leia_on_success));
            badge.setVisible(n > 0);
            if (n > 0) badge.setNumber(n);
        });
    }

    private void observeTradeBadge(long userId) {
        androidx.lifecycle.MediatorLiveData<Integer> needs = new androidx.lifecycle.MediatorLiveData<>();
        java.util.List<?>[] lists = new java.util.List<?>[2];
        androidx.lifecycle.LiveData<java.util.List<br.ufms.leiaufms.data.local.model.PedidoRow>> rec =
                AppDatabase.get(this).pedidoDao().received(userId);
        androidx.lifecycle.LiveData<java.util.List<br.ufms.leiaufms.data.local.model.PedidoRow>> sent =
                AppDatabase.get(this).pedidoDao().sent(userId);
        needs.addSource(rec, l -> { lists[0] = l; needs.setValue(anyNeeds(lists, userId)); });
        needs.addSource(sent, l -> { lists[1] = l; needs.setValue(anyNeeds(lists, userId)); });
        needs.observe(this, n -> {
            com.google.android.material.badge.BadgeDrawable badge = binding.bottomNav.getOrCreateBadge(R.id.tradesFragment);
            badge.setBackgroundColor(getColor(R.color.leia_tertiary));
            badge.setBadgeTextColor(getColor(R.color.leia_on_tertiary));
            badge.setVisible(n != null && n > 0);
            if (n != null && n > 0) badge.setNumber(n);
        });
    }

    @SuppressWarnings("unchecked")
    private static int anyNeeds(java.util.List<?>[] lists, long userId) {
        int n = 0;
        for (java.util.List<?> l : lists) {
            if (l == null) continue;
            for (Object o : l) {
                if (br.ufms.leiaufms.ui.common.Labels.needsAction(((br.ufms.leiaufms.data.local.model.PedidoRow) o).pedido, userId)) {
                    n++;
                }
            }
        }
        return n;
    }

    /** Aparece 60 min antes; a 30 min fica verde forte e a 10 min azul (mais forte), até 30 min depois. */
    private void renderMeeting() {
        meetingTick.removeCallbacks(meetingRefresh);
        com.google.android.material.card.MaterialCardView bar = binding.meeting.meetingBar;
        long now = System.currentTimeMillis();
        if (meeting == null || meeting.encontroEm - now > 60 * MIN || now - meeting.encontroEm > 30 * MIN) {
            bar.setVisibility(android.view.View.GONE);
            if (meeting != null && meeting.encontroEm - now > 60 * MIN) {
                meetingTick.postDelayed(meetingRefresh, meeting.encontroEm - now - 60 * MIN);
            }
            return;
        }
        long diff = meeting.encontroEm - now;
        int mins = (int) Math.round(Math.abs(diff) / (double) MIN);
        String when = diff > MIN ? getString(R.string.meet_in, mins)
                : diff < -MIN ? getString(R.string.meet_late, mins) : getString(R.string.meet_now);
        boolean urgent = diff <= 10 * MIN;
        boolean soon = diff <= 30 * MIN;
        int bg = urgent ? R.color.leia_primary : soon ? R.color.leia_success : R.color.leia_success_container;
        int fg = urgent ? R.color.leia_on_primary : soon ? R.color.leia_on_meeting : R.color.leia_on_success_container;
        if (soon && !urgent) bg = R.color.leia_meeting_soon;
        bar.setCardBackgroundColor(getColor(bg));
        int fgc = getColor(fg);
        for (android.widget.TextView t : new android.widget.TextView[]{binding.meeting.meetWhen,
                binding.meeting.meetWhere, binding.meeting.meetOther}) {
            t.setTextColor(fgc);
            androidx.core.widget.TextViewCompat.setCompoundDrawableTintList(t, android.content.res.ColorStateList.valueOf(fgc));
        }
        androidx.core.widget.TextViewCompat.setCompoundDrawableTintList(binding.meeting.meetMe,
                android.content.res.ColorStateList.valueOf(fgc));
        binding.meeting.meetMe.setTextColor(fgc);
        binding.meeting.meetChevron.setColorFilter(fgc);
        binding.meeting.meetWhen.setText(when + " · " + br.ufms.leiaufms.ui.common.TimeText.hour(meeting.encontroEm));
        binding.meeting.meetWhere.setText(meeting.pontoNome);
        br.ufms.leiaufms.ui.common.Covers.bind(binding.meeting.meetCover.coverImage, binding.meeting.meetCover.coverText,
                meeting.capaPath, meeting.titulo);
        String other = "";
        if (meeting.outroLat != null && meeting.pontoLat != null) {
            float d = br.ufms.leiaufms.core.location.WalkEstimate.distance(meeting.outroLat, meeting.outroLng,
                    meeting.pontoLat, meeting.pontoLng);
            other = getString(R.string.meet_other_dist, meeting.outroNome,
                    br.ufms.leiaufms.ui.common.Labels.distance(this, d).replaceFirst("^a ", ""));
            binding.meeting.meetOther.setVisibility(android.view.View.VISIBLE);
            binding.meeting.meetOther.setText(other);
        } else {
            binding.meeting.meetOther.setVisibility(android.view.View.GONE);
        }
        String meText = myEta();
        binding.meeting.meetMe.setVisibility(meText == null ? android.view.View.GONE : android.view.View.VISIBLE);
        binding.meeting.meetMe.setText(meText);
        refreshMyLocation();
        bar.setContentDescription(getString(R.string.meet_bar_description, when, meeting.pontoNome,
                meText == null ? "" : meText + ".", other));
        bar.setVisibility(android.view.View.VISIBLE);
        meetingTick.postDelayed(meetingRefresh, 30_000);
    }

    // ---- Meu tempo até o ponto: só com a permissão já concedida (RNF-07), nunca pede aqui ----

    private android.location.Location myLocation;
    private long myLocationAt;
    private br.ufms.leiaufms.core.location.LocationProvider locationProvider;

    private String myEta() {
        if (myLocation == null || meeting == null || meeting.pontoLat == null) return null;
        float d = br.ufms.leiaufms.core.location.WalkEstimate.distance(myLocation.getLatitude(),
                myLocation.getLongitude(), meeting.pontoLat, meeting.pontoLng);
        if (d < 50) return getString(R.string.meet_me_there);
        if (!br.ufms.leiaufms.core.location.WalkEstimate.isWalkable(d)) {
            return getString(R.string.meet_me_far, br.ufms.leiaufms.ui.common.Labels.distance(this, d));
        }
        return getString(R.string.meet_me_walk, br.ufms.leiaufms.core.location.WalkEstimate.minutes(d));
    }

    private void refreshMyLocation() {
        if (System.currentTimeMillis() - myLocationAt < 60_000) return;
        if (!br.ufms.leiaufms.core.permission.PermissionHelper.anyGranted(this,
                br.ufms.leiaufms.core.location.LocationProvider.PERMISSIONS)) return;
        myLocationAt = System.currentTimeMillis();
        if (locationProvider == null) locationProvider = new br.ufms.leiaufms.core.location.LocationProvider(this);
        locationProvider.current(new br.ufms.leiaufms.core.location.LocationProvider.Callback() {
            @Override
            public void onLocation(android.location.Location location) {
                myLocation = location;
                if (binding != null && meeting != null) renderMeeting();
            }

            @Override
            public void onFailure(br.ufms.leiaufms.core.location.LocationProvider.Failure failure) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (locationProvider != null) locationProvider.cancel();
        meetingTick.removeCallbacksAndMessages(null);
        super.onDestroy();
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
