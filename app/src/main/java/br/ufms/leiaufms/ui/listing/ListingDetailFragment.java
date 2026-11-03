package br.ufms.leiaufms.ui.listing;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.location.LocationProvider;
import br.ufms.leiaufms.core.location.WalkEstimate;
import br.ufms.leiaufms.core.permission.PermissionHelper;
import br.ufms.leiaufms.data.local.entity.Anuncio;
import br.ufms.leiaufms.data.local.entity.Pedido;
import br.ufms.leiaufms.data.local.model.ExemplarRow;
import br.ufms.leiaufms.data.local.model.ListingRow;
import br.ufms.leiaufms.data.repository.ListingRepository;
import br.ufms.leiaufms.data.repository.TradeRepository;
import br.ufms.leiaufms.databinding.FragmentListingDetailBinding;
import br.ufms.leiaufms.ui.common.Covers;
import br.ufms.leiaufms.ui.common.ImageLoader;
import br.ufms.leiaufms.ui.common.Labels;
import br.ufms.leiaufms.ui.common.TimeText;
import br.ufms.leiaufms.ui.profile.ProfileFragment;
import br.ufms.leiaufms.ui.trade.TradeDetailFragment;

/**
 * Detalhe do anúncio (briefing D2): decidir se vale pedir. Distância e "a pé" (RF-28) aparecem
 * antes do botão; sem permissão de localização a tela não pede, só explica.
 */
public class ListingDetailFragment extends Fragment {

    public static final String ARG_ID = "anuncioId";

    private FragmentListingDetailBinding binding;
    private ListingDetailViewModel viewModel;
    private LocationProvider locationProvider;
    private PermissionHelper permissions;
    private ListingRow current;
    private ListingDetailViewModel.Extra extra;
    private android.location.Location here;
    private br.ufms.leiaufms.ui.home.BookCardAdapter copiesAdapter;
    private long copiesObserved = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissions = new PermissionHelper(this);
        locationProvider = new LocationProvider(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentListingDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ListingDetailViewModel.class);
        viewModel.init(requireArguments().getLong(ARG_ID));
        binding.ownerAvatar.avatarInitials.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        copiesAdapter = new br.ufms.leiaufms.ui.home.BookCardAdapter(new br.ufms.leiaufms.ui.home.BookCardAdapter.Listener() {
            @Override
            public void onOpen(ListingRow row) {
                Bundle args = new Bundle();
                args.putLong(ARG_ID, row.anuncioId);
                NavHostFragment.findNavController(ListingDetailFragment.this).navigate(R.id.listingDetailFragment, args);
            }

            @Override
            public void onToggleWish(ListingRow row) {
                viewModel.toggleWish(row.obraId, current == null ? row.donoId : current.donoId);
            }
        });
        binding.copies.setAdapter(copiesAdapter);
        viewModel.row().observe(getViewLifecycleOwner(), r -> {
            current = r;
            if (r == null) return;
            renderRow(r);
            viewModel.refresh(r.donoId);
        });
        viewModel.extra().observe(getViewLifecycleOwner(), e -> {
            extra = e;
            renderActions();
        });
        viewModel.events().observe(getViewLifecycleOwner(), this::onEvent);
        if (PermissionHelper.anyGranted(requireContext(), LocationProvider.PERMISSIONS)) {
            locationProvider.current(new LocationProvider.Callback() {
                @Override
                public void onLocation(android.location.Location location) {
                    here = location;
                    if (binding != null && current != null) renderDistance(current);
                }

                @Override
                public void onFailure(LocationProvider.Failure failure) {
                }
            });
        }
    }

    private void renderRow(ListingRow r) {
        binding.title.setText(r.titulo);
        binding.author.setText(r.autor);
        binding.buttonObra.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putLong(br.ufms.leiaufms.ui.book.ObraFragment.ARG_ID, r.obraId);
            NavHostFragment.findNavController(this).navigate(R.id.obraFragment, args);
        });
        Covers.bind(binding.cover.coverImage, binding.cover.coverText, r.imagem(), r.titulo);
        binding.facts.removeAllViews();
        addFact(Anuncio.MODALIDADE_DOACAO.equals(r.modalidade) ? R.drawable.ic_donate : R.drawable.ic_swap,
                Labels.modality(requireContext(), r.modalidade), true);
        addFact(R.drawable.ic_star_border,
                getString(R.string.label_condition) + ": " + Labels.condition(requireContext(), r.conservacao), false);
        if (r.edicao != null) addFact(R.drawable.ic_book, r.edicao, false);
        binding.hero.back.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.hero.share.setOnClickListener(v -> share(r));
        binding.barText.setText(Anuncio.MODALIDADE_DOACAO.equals(r.modalidade) ? R.string.bar_free
                : Anuncio.MODALIDADE_TROCA.equals(r.modalidade) ? R.string.bar_trade : R.string.bar_both);
        binding.hero.heart.setOnClickListener(v -> {
            v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(120)
                    .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()).start();
            viewModel.toggleWish(r.obraId, r.donoId);
        });
        if (copiesObserved != r.obraId) {
            copiesObserved = r.obraId;
            viewModel.copies(r.obraId).observe(getViewLifecycleOwner(), list -> {
                java.util.List<ListingRow> others = new java.util.ArrayList<>();
                if (list != null) for (ListingRow x : list) if (x.anuncioId != r.anuncioId) others.add(x);
                copiesAdapter.submitList(others);
                boolean any = !others.isEmpty();
                binding.copiesTitle.setVisibility(any ? View.VISIBLE : View.GONE);
                binding.copies.setVisibility(any ? View.VISIBLE : View.GONE);
            });
        }
        binding.ownerName.setText(r.donoNome);
        boolean hasPhoto = ImageLoader.load(binding.ownerAvatar.avatarImage, r.donoFoto,
                getResources().getDimensionPixelSize(R.dimen.avatar_small));
        binding.ownerAvatar.avatarInitials.setVisibility(hasPhoto ? View.GONE : View.VISIBLE);
        binding.ownerAvatar.avatarInitials.setText(ImageLoader.initials(r.donoNome));
        binding.ownerCard.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putLong(ProfileFragment.ARG_USER_ID, r.donoId);
            NavHostFragment.findNavController(this).navigate(R.id.userProfileFragment, args);
        });
        renderDistance(r);
    }

    /** Linha "ícone + fato"; a modalidade fica em azul por ser o que decide o pedido. */
    private void addFact(int icon, String text, boolean highlight) {
        android.widget.TextView row = new android.widget.TextView(requireContext());
        row.setText(text);
        row.setTextAppearance(R.style.TextAppearance_Leia_Body);
        int color = requireContext().getColor(highlight ? R.color.leia_primary : R.color.leia_on_background);
        row.setTextColor(color);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setMinHeight(getResources().getDimensionPixelSize(R.dimen.touch_target));
        row.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.space_s));
        row.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0);
        androidx.core.widget.TextViewCompat.setCompoundDrawableTintList(row,
                android.content.res.ColorStateList.valueOf(color));
        binding.facts.addView(row);
    }

    /** Compartilhar o anúncio com amigos pela folha do Android (feedback 03/11). */
    private void share(ListingRow r) {
        String text = getString(R.string.share_listing_text, r.titulo, r.autor,
                Labels.modality(requireContext(), r.modalidade),
                Labels.condition(requireContext(), r.conservacao),
                r.pontoNome == null ? getString(R.string.share_listing_no_point) : r.pontoNome);
        android.content.Intent send = new android.content.Intent(android.content.Intent.ACTION_SEND)
                .setType("text/plain").putExtra(android.content.Intent.EXTRA_TEXT, text);
        startActivity(android.content.Intent.createChooser(send, getString(R.string.action_share)));
    }

    private void renderDistance(ListingRow r) {
        Double lat = r.latitude();
        Double lng = r.longitude();
        if (r.pontoNome != null) binding.locationText.setText(getString(R.string.location_point, r.pontoNome));
        else if (lat != null) binding.locationText.setText(R.string.location_owner);
        else binding.locationText.setText(R.string.no_distance);
        if (lat == null || lng == null) {
            binding.distance.setVisibility(View.GONE);
            binding.walkNote.setVisibility(View.GONE);
        } else if (here == null) {
            binding.distance.setVisibility(View.GONE);
            binding.walkNote.setVisibility(View.VISIBLE);
            binding.walkNote.setText(R.string.location_hint_detail);
        } else {
            float d = WalkEstimate.distance(here.getLatitude(), here.getLongitude(), lat, lng);
            binding.distance.setVisibility(View.VISIBLE);
            binding.distance.setText(Labels.distance(requireContext(), d) + " · " + Labels.walk(requireContext(), d));
            binding.distance.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    WalkEstimate.isWalkable(d) ? R.drawable.ic_walk_small : 0, 0, 0, 0);
            binding.walkNote.setVisibility(View.VISIBLE);
            binding.walkNote.setText(R.string.walk_note);
        }
    }

    private void renderActions() {
        if (current == null || extra == null) return;
        if (extra.ownerMetrics != null) {
            String trades = getResources().getQuantityString(R.plurals.owner_trades, extra.ownerMetrics.trades, extra.ownerMetrics.trades);
            binding.ownerMeta.setText((current.donoCurso == null ? "" : current.donoCurso + " · ") + trades);
            String rating;
            if (extra.ownerMetrics.rating != null) {
                rating = String.format(new java.util.Locale("pt", "BR"), "%.1f", extra.ownerMetrics.rating);
                binding.ownerRating.setText(rating);
                binding.ownerRatingLabel.setText(R.string.trust_rating_label);
            } else {
                rating = getString(R.string.trust_new);
                binding.ownerRating.setText("—");
                binding.ownerRatingLabel.setText(R.string.trust_new);
            }
            binding.ownerCard.setContentDescription(getString(R.string.label_owner) + ": " + current.donoNome + ". "
                    + trades + ". " + getString(R.string.trust_rating_label) + " " + rating);
        }
        binding.hero.heart.setImageResource(extra.wished ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        binding.hero.heart.setColorFilter(requireContext().getColor(extra.wished ? R.color.leia_primary : R.color.leia_on_background));
        binding.hero.heart.setContentDescription(getString(extra.wished ? R.string.wish_remove : R.string.wish_add));
        binding.proofWishes.setVisibility(extra.wishCount > 0 ? View.VISIBLE : View.GONE);
        binding.proofWishes.setText(getResources().getQuantityString(R.plurals.wish_count, extra.wishCount, extra.wishCount));
        binding.proofHands.setVisibility(extra.hands > 0 ? View.VISIBLE : View.GONE);
        binding.proofHands.setText(getResources().getQuantityString(R.plurals.hands_count, extra.hands, extra.hands));
        binding.courses.removeAllViews();
        boolean hasCourses = extra.courses != null && !extra.courses.isEmpty();
        binding.coursesTitle.setVisibility(hasCourses ? View.VISIBLE : View.GONE);
        if (hasCourses) for (br.ufms.leiaufms.data.local.entity.Disciplina c : extra.courses) addCourse(c);
        binding.proofHands.setOnClickListener(v -> showTrajectory());
        binding.buttonPrimary.setVisibility(View.GONE);
        binding.buttonSecondary.setVisibility(View.GONE);
        binding.statusNote.setVisibility(View.GONE);
        binding.actionBar.setVisibility(View.VISIBLE);
        binding.barText.setVisibility(View.VISIBLE);
        if (current.donoId == viewModel.userId()) {
            binding.statusNote.setVisibility(View.VISIBLE);
            binding.statusNote.setText(R.string.own_listing);
            if (current.ativo) {
                primary(R.string.action_edit_listing, v -> {
                    Bundle args = new Bundle();
                    args.putLong(NewListingFragment.ARG_ID, current.anuncioId);
                    NavHostFragment.findNavController(this).navigate(R.id.newListingFragment, args);
                });
                secondary(R.string.action_close_listing, v -> confirmClose());
            }
            return;
        }
        if (extra.myActive != null) {
            binding.statusNote.setVisibility(View.VISIBLE);
            binding.statusNote.setText(R.string.already_requested);
            primary(R.string.action_view_request, v -> openTrade(extra.myActive.id));
            return;
        }
        if (!current.ativo || extra.reserved) {
            binding.statusNote.setVisibility(View.VISIBLE);
            binding.statusNote.setText(R.string.listing_closed);
            binding.actionBar.setVisibility(View.GONE);
            return;
        }
        // Com crédito: "Resgatar · 1 crédito". Sem crédito: "Pedir doação" (RN-CRED-04 continua valendo)
        int rescueLabel = extra.myCredits >= 1 ? R.string.action_rescue_credit : R.string.action_ask_donation;
        if (Anuncio.MODALIDADE_DOACAO.equals(current.modalidade)) {
            primary(rescueLabel, v -> askRescue());
        } else {
            primary(R.string.action_request_trade, v -> viewModel.loadOfferable());
            if (Anuncio.MODALIDADE_AMBAS.equals(current.modalidade)) secondary(rescueLabel, v -> askRescue());
        }
    }

    /** RF-17: por quantas mãos o exemplar passou, em linha do tempo (só nome, curso e semestre). */
    private void showTrajectory() {
        if (extra == null || extra.trajectory == null) return;
        String[] lines = new String[extra.trajectory.size()];
        for (int i = 0; i < lines.length; i++) {
            br.ufms.leiaufms.data.local.dao.ExemplarDao.TrajetoriaRow t = extra.trajectory.get(i);
            lines[i] = getString(R.string.trajectory_line, t.nome, t.curso, t.semestre, TimeText.date(t.desde));
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.trajectory_title)
                .setItems(lines, null)
                .setPositiveButton(R.string.action_keep, null)
                .show();
    }

    private void addCourse(br.ufms.leiaufms.data.local.entity.Disciplina d) {
        com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(requireContext());
        chip.setText(d.nome);
        chip.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putLong(br.ufms.leiaufms.ui.book.DisciplinaFragment.ARG_ID, d.id);
            NavHostFragment.findNavController(this).navigate(R.id.disciplinaFragment, args);
        });
        chip.setChipIconResource(R.drawable.ic_school);
        chip.setChipIconVisible(true);
        binding.courses.addView(chip);
    }

    private void primary(int text, View.OnClickListener l) {
        binding.buttonPrimary.setVisibility(View.VISIBLE);
        binding.buttonPrimary.setText(text);
        binding.buttonPrimary.setOnClickListener(l);
    }

    private void secondary(int text, View.OnClickListener l) {
        binding.barText.setVisibility(View.GONE);
        // Dois botões dividem a barra igualmente
        for (View b : new View[]{binding.buttonSecondary, binding.buttonPrimary}) {
            android.widget.LinearLayout.LayoutParams lp = (android.widget.LinearLayout.LayoutParams) b.getLayoutParams();
            lp.width = 0;
            lp.weight = 1;
            b.setLayoutParams(lp);
        }
        binding.buttonSecondary.setVisibility(View.VISIBLE);
        binding.buttonSecondary.setText(text);
        binding.buttonSecondary.setOnClickListener(l);
    }

    private void askRescue() {
        int credits = extra == null ? 0 : extra.myCredits;
        String message = credits >= 1 ? getString(R.string.request_rescue_with_credit, credits)
                : getString(R.string.request_no_credit);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(credits >= 1 ? R.string.request_rescue_title : R.string.ask_donation_title)
                .setMessage(message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_send_request, (d, w) -> viewModel.request(Pedido.TIPO_RESGATE, null))
                .show();
    }

    @SuppressWarnings("unchecked")
    private void onEvent(ListingDetailViewModel.Event e) {
        if (e == null) return;
        viewModel.consume();
        switch (e.kind) {
            case "offerable":
                chooseOffer((List<ExemplarRow>) e.value);
                break;
            case "request":
                onRequestResult((TradeRepository.Result) e.value);
                break;
            case "close":
                ListingRepository.Result r = (ListingRepository.Result) e.value;
                if (r == ListingRepository.Result.OK) {
                    snack(R.string.listing_closed_done);
                    NavHostFragment.findNavController(this).popBackStack();
                } else if (r == ListingRepository.Result.BLOCKED) {
                    snack(R.string.listing_blocked);
                } else {
                    snack(R.string.error_generic);
                }
                break;
            default:
                break;
        }
    }

    private void chooseOffer(List<ExemplarRow> list) {
        if (list == null || list.isEmpty()) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.request_trade_title)
                    .setMessage(R.string.request_trade_none)
                    .setNegativeButton(R.string.action_cancel, null)
                    .setPositiveButton(R.string.action_new_listing, (d, w) ->
                            NavHostFragment.findNavController(this).navigate(R.id.newListingFragment))
                    .show();
            return;
        }
        String[] items = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            ExemplarRow x = list.get(i);
            items[i] = x.edicao == null ? x.titulo : x.titulo + " (" + x.edicao + ")";
        }
        final int[] chosen = {0};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.request_trade_title)
                .setSingleChoiceItems(items, 0, (d, which) -> chosen[0] = which)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_send_request, (d, w) ->
                        viewModel.request(Pedido.TIPO_TROCA_DIRETA, list.get(chosen[0]).exemplarId))
                .show();
    }

    private void onRequestResult(TradeRepository.Result r) {
        if (r == null) {
            snack(R.string.error_generic);
            return;
        }
        switch (r) {
            case OK:
                snack(R.string.request_sent);
                if (current != null) viewModel.refresh(current.donoId);
                askNotificationPermissionOnce();
                break;
            case OWN_LISTING:
                snack(R.string.error_own_listing);
                break;
            case LIMIT:
                snack(R.string.error_request_limit);
                break;
            case ALREADY_REQUESTED:
                snack(R.string.error_already_requested);
                break;
            case OFFER_UNAVAILABLE:
                snack(R.string.error_offer_unavailable);
                break;
            case WRONG_TYPE:
                snack(R.string.error_wrong_type);
                break;
            default:
                snack(R.string.error_listing_closed);
                break;
        }
    }

    /** RF-34: a permissão de notificação é pedida na primeira ação relevante, não na abertura. */
    private void askNotificationPermissionOnce() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;
        String p = Manifest.permission.POST_NOTIFICATIONS;
        if (PermissionHelper.isGranted(requireContext(), p)
                || PermissionHelper.isPermanentlyDenied(requireActivity(), p)) return;
        permissions.request(new PermissionHelper.Callback() {
            @Override
            public void onGranted() {
            }

            @Override
            public void onDenied(boolean permanently) {
                if (binding != null) snack(R.string.notifications_denied);
            }
        }, p);
    }

    private void confirmClose() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.close_listing_title)
                .setMessage(R.string.close_listing_message)
                .setNegativeButton(R.string.action_keep, null)
                .setPositiveButton(R.string.action_close_listing, (d, w) -> viewModel.close())
                .show();
    }

    private void openTrade(long pedidoId) {
        Bundle args = new Bundle();
        args.putLong(TradeDetailFragment.ARG_ID, pedidoId);
        NavHostFragment.findNavController(this).navigate(R.id.tradeDetailFragment, args);
    }

    private void snack(int text) {
        Snackbar.make(requireActivity().findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                .setAnchorView(requireActivity().findViewById(R.id.bottom_nav)).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        locationProvider.cancel();
        binding = null;
    }
}
