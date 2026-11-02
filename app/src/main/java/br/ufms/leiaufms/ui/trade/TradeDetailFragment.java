package br.ufms.leiaufms.ui.trade;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import br.ufms.leiaufms.R;
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
            boolean first = current == null;
            current = r;
            render(r);
            if (first) {
                long other = r.pedido.donoId == viewModel.userId() ? r.pedido.solicitanteId : r.pedido.donoId;
                viewModel.loadParty(other, r.pedido.solicitanteId);
            }
        });
        viewModel.party().observe(getViewLifecycleOwner(), this::renderParty);
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

        binding.state.setText(Labels.state(requireContext(), p.estado));
        binding.state.setCompoundDrawablesRelativeWithIntrinsicBounds(Labels.stateIcon(p.estado), 0, 0, 0);
        String next = Labels.nextStep(requireContext(), p, me);
        binding.next.setVisibility(next == null ? View.GONE : View.VISIBLE);
        binding.next.setText(next);
        boolean hasMeeting = p.encontroEm != null && r.pontoNome != null;
        binding.meeting.setVisibility(PedidoEstado.ACEITO.equals(p.estado) || hasMeeting ? View.VISIBLE : View.GONE);
        binding.meeting.setText(hasMeeting ? getString(R.string.trade_meeting, r.pontoNome, TimeText.dateTime(p.encontroEm))
                : getString(R.string.trade_no_meeting));
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
            show(binding.buttonCancel, R.string.action_cancel_request, v -> confirmCancel());
        } else if (PedidoEstado.EM_DISPUTA.equals(p.estado) || PedidoEstado.CONCLUIDO.equals(p.estado)
                || PedidoEstado.CANCELADO.equals(p.estado)) {
            if (p.aceitoEm != null) show(binding.buttonChat, R.string.action_open_chat, v -> openChat());
        }
    }

    private void renderParty(TradeDetailViewModel.Party party) {
        if (party == null || party.user == null || current == null) return;
        binding.partyName.setText(party.user.nome);
        StringBuilder meta = new StringBuilder(party.user.curso);
        meta.append(" · ").append(getString(R.string.owner_trades, party.metrics.trades));
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
            if (p.semCredito) text += "\n" + getString(R.string.trade_no_credit);
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
        Snackbar.make(binding.getRoot(), R.string.chat_soon, Snackbar.LENGTH_SHORT).show();
    }

    private void onResult(TradeRepository.Result r) {
        int msg;
        if (r == TradeRepository.Result.OK) {
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
        binding = null;
    }
}
