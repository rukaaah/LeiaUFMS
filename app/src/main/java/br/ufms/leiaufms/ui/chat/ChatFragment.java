package br.ufms.leiaufms.ui.chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;

import br.ufms.leiaufms.AppConfig;
import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.model.PedidoRow;
import br.ufms.leiaufms.data.pedido.PedidoEstado;
import br.ufms.leiaufms.data.repository.SocialRepository;
import br.ufms.leiaufms.databinding.FragmentChatBinding;

/**
 * Chat do pedido (RF-40, PC-12): só existe com pedido ACEITO; depois do encerramento fica somente
 * leitura. Mensagens prontas para combinar a logística em um toque; texto livre até 300 caracteres.
 */
public class ChatFragment extends Fragment {

    public static final String ARG_PEDIDO = "pedidoId";
    private static final int[] QUICK = {R.string.quick_1, R.string.quick_2, R.string.quick_3, R.string.quick_4};

    private FragmentChatBinding binding;
    private long pedidoId;
    private long me;
    private boolean open;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        pedidoId = requireArguments().getLong(ARG_PEDIDO);
        me = SessionManager.get(requireContext()).currentUserId();
        AppDatabase db = AppDatabase.get(requireContext());
        MessageAdapter adapter = new MessageAdapter(me);
        LinearLayoutManager lm = new LinearLayoutManager(requireContext());
        lm.setStackFromEnd(true);
        binding.list.setLayoutManager(lm);
        binding.list.setAdapter(adapter);

        db.pedidoDao().observeRow(pedidoId).observe(getViewLifecycleOwner(), this::renderState);
        db.socialDao().messages(pedidoId).observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list, () -> {
                if (binding != null && adapter.getItemCount() > 0) binding.list.scrollToPosition(adapter.getItemCount() - 1);
            });
            binding.empty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });

        for (int res : QUICK) {
            Chip chip = new Chip(requireContext());
            chip.setText(res);
            chip.setOnClickListener(v -> send(getString(res), true));
            binding.quick.addView(chip);
        }
        binding.inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int len = s.toString().trim().length();
                binding.buttonSend.setEnabled(open && len > 0 && len <= AppConfig.CHAT_MAX_CHARS);
                binding.layoutMessage.setError(len > AppConfig.CHAT_MAX_CHARS ? getString(R.string.chat_too_long) : null);
            }
        });
        binding.buttonSend.setOnClickListener(v -> {
            Editable e = binding.inputMessage.getText();
            send(e == null ? "" : e.toString(), false);
        });
    }

    private void renderState(PedidoRow r) {
        if (r == null) return;
        String other = r.pedido.donoId == me ? r.solicitanteNome : r.donoNome;
        binding.header.setText(getString(R.string.chat_header, r.titulo, other));
        open = PedidoEstado.ACEITO.equals(r.pedido.estado);
        binding.composer.setVisibility(open ? View.VISIBLE : View.GONE);
        if (open) {
            binding.status.setVisibility(View.GONE);
        } else {
            binding.status.setVisibility(View.VISIBLE);
            binding.status.setText(PedidoEstado.PENDENTE.equals(r.pedido.estado) ? R.string.chat_locked : R.string.chat_closed);
        }
    }

    private void send(String text, boolean pronta) {
        if (!open) return;
        android.content.Context app = requireContext().getApplicationContext();
        AppExecutors.io().execute(() -> {
            SocialRepository.Result r = new SocialRepository(app).sendMessage(pedidoId, me, text, pronta);
            AppExecutors.main(() -> {
                if (binding == null) return;
                if (r == SocialRepository.Result.OK) {
                    if (!pronta) binding.inputMessage.setText(null);
                } else if (r == SocialRepository.Result.TOO_LONG) {
                    binding.layoutMessage.setError(getString(R.string.chat_too_long));
                } else if (r == SocialRepository.Result.CLOSED) {
                    Snackbar.make(binding.getRoot(), R.string.chat_closed, Snackbar.LENGTH_LONG).show();
                } else if (r != SocialRepository.Result.EMPTY) {
                    Snackbar.make(binding.getRoot(), R.string.error_generic, Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
