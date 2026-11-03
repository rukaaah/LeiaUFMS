package br.ufms.leiaufms.ui.trade;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import br.ufms.leiaufms.AppConfig;
import br.ufms.leiaufms.R;
import br.ufms.leiaufms.data.local.entity.Avaliacao;
import br.ufms.leiaufms.databinding.DialogRatingBinding;
import br.ufms.leiaufms.ui.chat.ChatFragment;
import br.ufms.leiaufms.data.local.entity.Pedido;
import br.ufms.leiaufms.data.local.model.PedidoRow;
import br.ufms.leiaufms.data.pedido.PedidoEstado;
import br.ufms.leiaufms.data.repository.TradeRepository;
import br.ufms.leiaufms.databinding.FragmentTradeDetailBinding;
import br.ufms.leiaufms.ui.common.Covers;
import br.ufms.leiaufms.ui.common.ImageLoader;
import br.ufms.leiaufms.ui.common.Labels;
import br.ufms.leiaufms.ui.common.TimeText;
import br.ufms.leiaufms.ui.listing.ListingDetailFragment;
import br.ufms.leiaufms.ui.meeting.MeetingFragment;
import br.ufms.leiaufms.ui.profile.ProfileFragment;

/**
 * Pedido (briefing D2): mostra o estado com o próximo passo e só as ações válidas para quem
 * está olhando. Recusar, cancelar e confirmar pedem confirmação.
 */
public class TradeDetailFragment extends Fragment {

    public static final String ARG_ID = "pedidoId";

    private FragmentTradeDetailBinding binding;
    private TradeDetailViewModel viewModel;
    private PedidoRow current;
    private boolean isModerator;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTradeDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(TradeDetailViewModel.class);
        viewModel.init(requireArguments().getLong(ARG_ID));
        binding.partyAvatar.avatarInitials.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        viewModel.row().observe(getViewLifecycleOwner(), r -> {
            if (r == null) return;
            boolean reload = current == null || !current.pedido.estado.equals(r.pedido.estado);
            current = r;
            render(r);
            if (reload) {
                long other = r.pedido.donoId == viewModel.userId() ? r.pedido.solicitanteId : r.pedido.donoId;
                viewModel.loadParty(other, r.pedido.solicitanteId);
            }
        });
        viewModel.party().observe(getViewLifecycleOwner(), this::renderParty);
        viewModel.myRating().observe(getViewLifecycleOwner(), this::renderRating);
        viewModel.moderator().observe(getViewLifecycleOwner(), mod -> {
            isModerator = Boolean.TRUE.equals(mod);
            if (current != null) render(current);
        });
        viewModel.result().observe(getViewLifecycleOwner(), res -> {
            if (res == null) return;
            viewModel.consume();
            onResult(res[0]);
        });
    }

    private void render(PedidoRow r) {
        Pedido p = r.pedido;
        long me = viewModel.userId();
        boolean owner = p.donoId == me;
        binding.title.setText(r.titulo);
        binding.author.setText(r.autor);
        Covers.bind(binding.cover.coverImage, binding.cover.coverText, r.capaPath, r.titulo);
        binding.type.setText(Labels.isRescue(p) ? R.string.trade_type_rescue : R.string.trade_type_direct);
        binding.offer.setVisibility(r.oferecidoTitulo == null ? View.GONE : View.VISIBLE);
        if (r.oferecidoTitulo != null) binding.offer.setText(getString(R.string.trade_offer, r.oferecidoTitulo));
        binding.book.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putLong(ListingDetailFragment.ARG_ID, p.anuncioId);
            NavHostFragment.findNavController(this).navigate(R.id.listingDetailFragment, args);
        });

        int step = br.ufms.leiaufms.ui.common.TradeProgressView.stepOf(p);
        binding.progress.setVisibility(step >= 0 ? View.VISIBLE : View.GONE);
        if (step >= 0) binding.progress.setStep(step, PedidoEstado.CONCLUIDO.equals(p.estado));
        binding.state.setText(Labels.state(requireContext(), p.estado));
        binding.state.setCompoundDrawablesRelativeWithIntrinsicBounds(Labels.stateIcon(p.estado), 0, 0, 0);
        String next = Labels.nextStep(requireContext(), p, me);
        binding.next.setVisibility(next == null ? View.GONE : View.VISIBLE);
        binding.next.setText(next);
        boolean hasMeeting = p.encontroEm != null && r.pontoNome != null;
        binding.meeting.setVisibility(PedidoEstado.ACEITO.equals(p.estado) || hasMeeting ? View.VISIBLE : View.GONE);
        binding.meeting.setText(hasMeeting ? getString(R.string.trade_meeting, r.pontoNome, TimeText.dateTime(p.encontroEm))
                : getString(R.string.trade_no_meeting));
        renderLive(r);
        binding.reason.setVisibility(p.motivo == null ? View.GONE : View.VISIBLE);
        if (p.motivo != null) binding.reason.setText(getString(R.string.trade_reason, p.motivo));
        binding.partyTitle.setText(owner ? R.string.trade_requester_title : R.string.label_owner);

        // Ações válidas para o estado e o papel de quem olha
        hide(binding.buttonPrimary);
        hide(binding.buttonSecondary);
        hide(binding.buttonChat);
        hide(binding.buttonDispute);
        hide(binding.buttonCancel);
        if (PedidoEstado.PENDENTE.equals(p.estado)) {
            if (owner) {
                show(binding.buttonPrimary, R.string.action_accept, v -> viewModel.run("accept"));
                show(binding.buttonSecondary, R.string.action_decline, v -> confirmDecline());
            } else {
                show(binding.buttonCancel, R.string.action_cancel_request, v -> confirmCancel());
            }
        } else if (PedidoEstado.ACEITO.equals(p.estado)) {
            boolean mine = owner ? p.confirmadoDono : p.confirmadoSolicitante;
            if (p.encontroEm == null) {
                show(binding.buttonPrimary, R.string.action_schedule, v -> openMeeting());
            } else {
                if (!mine) {
                    int label = !Labels.isRescue(p) ? R.string.action_confirm_exchange
                            : owner ? R.string.action_confirm_delivered : R.string.action_confirm_received;
                    show(binding.buttonPrimary, label, v -> confirmDelivery());
                }
                show(binding.buttonSecondary, R.string.action_reschedule, v -> openMeeting());
            }
            show(binding.buttonChat, R.string.action_open_chat, v -> openChat());
            show(binding.buttonDispute, R.string.action_open_dispute, v -> confirmDispute());
            show(binding.buttonCancel, R.string.action_cancel_request, v -> confirmCancel());
        } else if (PedidoEstado.EM_DISPUTA.equals(p.estado) || PedidoEstado.CONCLUIDO.equals(p.estado)
                || PedidoEstado.CANCELADO.equals(p.estado)) {
            if (p.aceitoEm != null) show(binding.buttonChat, R.string.action_open_chat, v -> openChat());
            if (isModerator && PedidoEstado.EM_DISPUTA.equals(p.estado)) {
                show(binding.buttonPrimary, R.string.action_resolve_conclude, v -> confirmResolve(true));
                show(binding.buttonSecondary, R.string.action_resolve_cancel, v -> confirmResolve(false));
            }
            if (PedidoEstado.CONCLUIDO.equals(p.estado)) {
                viewModel.loadMyRating();
                // Conclusão + recompensa (docs/08 §2): convite para contar no mural
                show(binding.buttonSecondary, R.string.action_tell_feed, v -> {
                    Bundle args = new Bundle();
                    args.putString(br.ufms.leiaufms.ui.feed.NewPostFragment.ARG_TYPE,
                            br.ufms.leiaufms.data.local.entity.PostMural.TIPO_TROCA_CONCLUIDA);
                    args.putString(br.ufms.leiaufms.ui.feed.NewPostFragment.ARG_TITLE, r.titulo);
                    args.putString(br.ufms.leiaufms.ui.feed.NewPostFragment.ARG_AUTHOR, r.autor);
                    NavHostFragment.findNavController(this).navigate(R.id.newPostFragment, args);
                });
            }
        }
    }

    // ---- RF-41: localização só nos 10 min antes do encontro (até 30 min depois) ----

    private final android.os.Handler liveHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private br.ufms.leiaufms.core.location.LocationProvider liveProvider;
    private br.ufms.leiaufms.core.permission.PermissionHelper livePermissions;
    private boolean liveObserving;
    private final Runnable liveTick = new Runnable() {
        @Override
        public void run() {
            pushLocation();
            liveHandler.postDelayed(this, 60_000);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        livePermissions = new br.ufms.leiaufms.core.permission.PermissionHelper(this);
        liveProvider = new br.ufms.leiaufms.core.location.LocationProvider(requireContext());
    }

    private void renderLive(PedidoRow r) {
        Pedido p = r.pedido;
        boolean show = PedidoEstado.ACEITO.equals(p.estado) && p.encontroEm != null;
        binding.liveBox.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) {
            stopLive();
            return;
        }
        long now = System.currentTimeMillis();
        boolean open = br.ufms.leiaufms.data.pedido.MeetingWindow.isOpen(now, p.encontroEm);
        long me = viewModel.userId();
        long otherId = p.donoId == me ? p.solicitanteId : p.donoId;
        String otherName = p.donoId == me ? r.solicitanteNome : r.donoNome;
        if (!open) {
            boolean ended = now > p.encontroEm;
            binding.liveText.setText(ended ? getString(R.string.live_ended) : getString(R.string.live_closed,
                    TimeText.dateTime(br.ufms.leiaufms.data.pedido.MeetingWindow.opensAt(p.encontroEm))));
            binding.liveSwitch.setVisibility(View.GONE);
            binding.liveOther.setVisibility(View.GONE);
            stopLive();
            if (!ended) liveHandler.postDelayed(() -> { if (binding != null && current != null) renderLive(current); },
                    Math.max(1000, br.ufms.leiaufms.data.pedido.MeetingWindow.opensAt(p.encontroEm) - now));
            return;
        }
        binding.liveSwitch.setVisibility(View.VISIBLE);
        binding.liveSwitch.setText(getString(R.string.live_switch, otherName));
        binding.liveText.setText(binding.liveSwitch.isChecked() ? getString(R.string.live_on) : "");
        binding.liveSwitch.setOnCheckedChangeListener((b, checked) -> {
            if (checked) startLive();
            else {
                stopLive();
                long pid = p.id;
                android.content.Context app = requireContext().getApplicationContext();
                br.ufms.leiaufms.core.util.AppExecutors.io().execute(() ->
                        br.ufms.leiaufms.data.local.AppDatabase.get(app).localizacaoDao().stop(pid, me));
                binding.liveText.setText("");
            }
        });
        if (!liveObserving) {
            liveObserving = true;
            br.ufms.leiaufms.data.local.AppDatabase.get(requireContext()).localizacaoDao().observe(p.id, otherId)
                    .observe(getViewLifecycleOwner(), loc -> renderOther(loc, otherName));
            // Ao voltar à tela com o compartilhamento ligado, retoma as atualizações
            br.ufms.leiaufms.data.local.AppDatabase.get(requireContext()).localizacaoDao().observe(p.id, me)
                    .observe(getViewLifecycleOwner(), mine -> {
                        if (binding == null || mine == null || binding.liveSwitch.isChecked()) return;
                        binding.liveSwitch.setChecked(true);
                    });
        }
    }

    private void renderOther(br.ufms.leiaufms.data.local.entity.LocalizacaoEncontro loc, String otherName) {
        if (binding == null || current == null) return;
        binding.liveOther.setVisibility(View.VISIBLE);
        if (loc == null || current.pontoLat == null) {
            binding.liveOther.setText(getString(R.string.live_other_none, otherName));
            return;
        }
        float d = br.ufms.leiaufms.core.location.WalkEstimate.distance(loc.lat, loc.lng, current.pontoLat, current.pontoLng);
        binding.liveOther.setText(getString(R.string.live_other, otherName,
                Labels.distance(requireContext(), d).replaceFirst("^a ", ""), TimeText.ago(requireContext(), loc.atualizadaEm)));
    }

    private void startLive() {
        livePermissions.request(new br.ufms.leiaufms.core.permission.PermissionHelper.Callback() {
            @Override
            public void onGranted() {
                if (binding == null) return;
                binding.liveText.setText(R.string.live_on);
                liveHandler.removeCallbacks(liveTick);
                liveHandler.post(liveTick);
            }

            @Override
            public void onDenied(boolean permanently) {
                if (binding == null) return;
                binding.liveSwitch.setChecked(false);
                binding.liveText.setText(R.string.location_denied);
            }
        }, br.ufms.leiaufms.core.location.LocationProvider.PERMISSIONS);
    }

    private void pushLocation() {
        if (current == null || !br.ufms.leiaufms.data.pedido.MeetingWindow.isOpen(System.currentTimeMillis(),
                current.pedido.encontroEm)) {
            stopLive();
            return;
        }
        long pid = current.pedido.id;
        long me = viewModel.userId();
        android.content.Context app = requireContext().getApplicationContext();
        liveProvider.current(new br.ufms.leiaufms.core.location.LocationProvider.Callback() {
            @Override
            public void onLocation(android.location.Location l) {
                br.ufms.leiaufms.core.util.AppExecutors.io().execute(() -> {
                    br.ufms.leiaufms.data.local.entity.LocalizacaoEncontro e = new br.ufms.leiaufms.data.local.entity.LocalizacaoEncontro();
                    e.pedidoId = pid;
                    e.usuarioId = me;
                    e.lat = l.getLatitude();
                    e.lng = l.getLongitude();
                    e.atualizadaEm = System.currentTimeMillis();
                    br.ufms.leiaufms.data.local.AppDatabase.get(app).localizacaoDao().upsert(e);
                });
            }

            @Override
            public void onFailure(br.ufms.leiaufms.core.location.LocationProvider.Failure f) {
                if (binding != null) binding.liveText.setText(f == br.ufms.leiaufms.core.location.LocationProvider.Failure.DISABLED
                        ? R.string.location_disabled : R.string.location_unavailable);
            }
        });
    }

    private void stopLive() {
        liveHandler.removeCallbacksAndMessages(null);
        if (liveProvider != null) liveProvider.cancel();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Sem rastreio em segundo plano (RNF-07): ao sair da tela, para de atualizar.
        // A última posição fica visível para a outra parte até o fim da janela ou até desligar.
        stopLive();
    }

    private void renderParty(TradeDetailViewModel.Party party) {
        if (party == null || party.user == null || current == null) return;
        binding.partyName.setText(party.user.nome);
        StringBuilder meta = new StringBuilder(party.user.curso);
        meta.append(" · ").append(getResources().getQuantityString(R.plurals.owner_trades, party.metrics.trades, party.metrics.trades));
        if (party.metrics.rating != null) meta.append(" · ").append(getString(R.string.owner_rating, party.metrics.rating));
        binding.partyMeta.setText(meta);
        boolean hasPhoto = ImageLoader.load(binding.partyAvatar.avatarImage, party.user.fotoPath,
                getResources().getDimensionPixelSize(R.dimen.avatar_small));
        binding.partyAvatar.avatarInitials.setVisibility(hasPhoto ? View.GONE : View.VISIBLE);
        binding.partyAvatar.avatarInitials.setText(ImageLoader.initials(party.user.nome));
        binding.partyCard.setContentDescription(party.user.nome + ". " + meta);
        binding.partyCard.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putLong(ProfileFragment.ARG_USER_ID, party.user.id);
            NavHostFragment.findNavController(this).navigate(R.id.userProfileFragment, args);
        });
        Pedido p = current.pedido;
        boolean owner = p.donoId == viewModel.userId();
        if (Labels.isRescue(p) && owner) {
            binding.creditNote.setVisibility(View.VISIBLE);
            String text = getString(R.string.trade_requester_balance, party.user.nome, party.requesterCredits);
            if (p.semCredito && PedidoEstado.PENDENTE.equals(p.estado)) text += "\n" + getString(R.string.trade_no_credit);
            binding.creditNote.setText(text);
        } else if (Labels.isRescue(p) && p.semCredito && PedidoEstado.PENDENTE.equals(p.estado)) {
            binding.creditNote.setVisibility(View.VISIBLE);
            binding.creditNote.setText(R.string.trade_no_credit);
        } else {
            binding.creditNote.setVisibility(View.GONE);
        }
    }

    private void show(com.google.android.material.button.MaterialButton b, int text, View.OnClickListener l) {
        b.setVisibility(View.VISIBLE);
        b.setText(text);
        b.setOnClickListener(l);
    }

    private void hide(View v) {
        v.setVisibility(View.GONE);
    }

    private void confirmDecline() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.decline_title)
                .setMessage(R.string.decline_message)
                .setNegativeButton(R.string.action_keep, null)
                .setPositiveButton(R.string.action_decline, (d, w) -> viewModel.run("decline"))
                .show();
    }

    private void confirmCancel() {
        TextInputLayout layout = new TextInputLayout(requireContext());
        TextInputEditText input = new TextInputEditText(layout.getContext());
        input.setHint(R.string.label_reason);
        input.setMaxLines(3);
        input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(120)});
        layout.addView(input);
        FrameLayout box = new FrameLayout(requireContext());
        int pad = getResources().getDimensionPixelSize(R.dimen.space_l);
        box.setPadding(pad, 0, pad, 0);
        box.addView(layout);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.cancel_title)
                .setMessage(R.string.cancel_message)
                .setView(box)
                .setNegativeButton(R.string.action_keep, null)
                .setPositiveButton(R.string.action_cancel_request, (d, w) -> viewModel.cancel(text(input)))
                .show();
    }

    /** RF-26: disputa pede motivo e confirmação (ação destrutiva). */
    private void confirmDispute() {
        TextInputLayout layout = new TextInputLayout(requireContext());
        TextInputEditText input = new TextInputEditText(layout.getContext());
        input.setHint(R.string.label_reason);
        input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(200)});
        layout.addView(input);
        FrameLayout box = new FrameLayout(requireContext());
        int pad = getResources().getDimensionPixelSize(R.dimen.space_l);
        box.setPadding(pad, 0, pad, 0);
        box.addView(layout);
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dispute_title)
                .setMessage(R.string.dispute_message)
                .setView(box)
                .setNegativeButton(R.string.action_keep, null)
                .setPositiveButton(R.string.action_open_dispute, null)
                .create();
        dialog.setOnShowListener(x -> dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String motivo = text(input);
                    if (motivo == null || motivo.trim().isEmpty()) {
                        layout.setError(getString(R.string.dispute_reason_required));
                        return;
                    }
                    viewModel.dispute(motivo.trim());
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private void confirmResolve(boolean concluir) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.resolve_title)
                .setMessage(R.string.resolve_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(concluir ? R.string.action_resolve_conclude : R.string.action_resolve_cancel,
                        (d, w) -> viewModel.resolve(concluir))
                .show();
    }

    private static String text(EditText e) {
        return e.getText() == null ? null : e.getText().toString();
    }

    private void confirmDelivery() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm_title)
                .setMessage(R.string.confirm_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm, (d, w) -> viewModel.run("confirm"))
                .show();
    }

    private void openMeeting() {
        Bundle args = new Bundle();
        args.putLong(MeetingFragment.ARG_PEDIDO, current.pedido.id);
        NavHostFragment.findNavController(this).navigate(R.id.meetingFragment, args);
    }

    private void openChat() {
        Bundle args = new Bundle();
        args.putLong(ChatFragment.ARG_PEDIDO, current.pedido.id);
        NavHostFragment.findNavController(this).navigate(R.id.chatFragment, args);
    }

    /** RF-25: botão de avaliação no pedido concluído (editável por 48 h). */
    private void renderRating(Avaliacao mine) {
        if (current == null || !PedidoEstado.CONCLUIDO.equals(current.pedido.estado)) return;
        boolean editable = mine == null
                || System.currentTimeMillis() - mine.criadaEm <= AppConfig.RATING_EDIT_WINDOW_MS;
        binding.ratingNote.setVisibility(mine == null ? View.GONE : View.VISIBLE);
        if (mine != null) binding.ratingNote.setText(getString(R.string.rating_mine, mine.nota));
        if (editable) {
            show(binding.buttonPrimary, mine == null ? R.string.action_rate : R.string.action_edit_rating,
                    v -> askRating(mine == null ? 0 : mine.nota));
        } else {
            hide(binding.buttonPrimary);
        }
    }

    private void askRating(int initial) {
        DialogRatingBinding d = DialogRatingBinding.inflate(getLayoutInflater());
        ImageButton[] stars = {d.star1, d.star2, d.star3, d.star4, d.star5};
        int[] chosen = {initial};
        Runnable paint = () -> {
            for (int i = 0; i < stars.length; i++) {
                stars[i].setImageResource(i < chosen[0] ? R.drawable.ic_star : R.drawable.ic_star_border);
            }
            d.label.setText(chosen[0] == 0 ? getString(R.string.rate_choose) : getString(R.string.rate_star, chosen[0]));
        };
        for (int i = 0; i < stars.length; i++) {
            final int n = i + 1;
            stars[i].setContentDescription(getString(R.string.rate_star, n));
            stars[i].setOnClickListener(v -> {
                chosen[0] = n;
                paint.run();
            });
        }
        paint.run();
        String name = binding.partyName.getText().toString();
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.rate_title, name))
                .setView(d.getRoot())
                .setNegativeButton(R.string.action_not_now, null)
                .setPositiveButton(R.string.action_send_rating, null)
                .create();
        dialog.setOnShowListener(x -> dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    if (chosen[0] == 0) return;
                    viewModel.rate(chosen[0]);
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private void onResult(TradeRepository.Result r) {
        int msg;
        if (r == TradeRepository.Result.OK && "rate".equals(viewModel.actionName)) {
            msg = R.string.rating_saved;
        } else if (r == TradeRepository.Result.OK && "dispute".equals(viewModel.actionName)) {
            msg = R.string.dispute_opened;
        } else if (r == TradeRepository.Result.OK && "resolve".equals(viewModel.actionName)) {
            msg = R.string.notif_dispute_resolved;
        } else if (r == TradeRepository.Result.OK) {
            switch (viewModel.actionName) {
                case "accept":
                    msg = R.string.trade_accepted_done;
                    break;
                case "decline":
                    msg = R.string.trade_declined_done;
                    break;
                case "cancel":
                    msg = R.string.trade_cancelled_done;
                    break;
                default:
                    msg = R.string.trade_confirmed_done;
            }
        } else {
            msg = R.string.error_invalid_state;
        }
        Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopLive();
        liveObserving = false;
        binding = null;
    }
}
