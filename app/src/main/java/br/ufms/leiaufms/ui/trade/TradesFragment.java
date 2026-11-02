package br.ufms.leiaufms.ui.trade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.List;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.model.PedidoRow;
import br.ufms.leiaufms.databinding.FragmentTradesBinding;
import br.ufms.leiaufms.ui.common.Labels;

/** Aba Trocas (briefing D2): pedidos que pedem minha ação primeiro, depois os demais. */
public class TradesFragment extends Fragment {

    private static final String KEY_SENT = "enviados";

    private FragmentTradesBinding binding;
    private LiveData<List<PedidoRow>> source;
    private long userId;
    private TradeAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTradesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        userId = SessionManager.get(requireContext()).currentUserId();
        adapter = new TradeAdapter(userId, row -> {
            Bundle args = new Bundle();
            args.putLong(TradeDetailFragment.ARG_ID, row.pedido.id);
            NavHostFragment.findNavController(this).navigate(R.id.tradeDetailFragment, args);
        });
        binding.list.setAdapter(adapter);
        binding.empty.emptyIcon.setImageResource(R.drawable.ic_swap);
        binding.empty.emptyTitle.setText(R.string.empty_trades_title);
        if (savedInstanceState != null && savedInstanceState.getBoolean(KEY_SENT)) binding.toggle.check(R.id.tab_sent);
        binding.toggle.addOnButtonCheckedListener((group, id, checked) -> {
            if (checked) observe();
        });
        observe();
    }

    private void observe() {
        if (source != null) source.removeObservers(getViewLifecycleOwner());
        boolean sent = binding.toggle.getCheckedButtonId() == R.id.tab_sent;
        AppDatabase db = AppDatabase.get(requireContext());
        source = sent ? db.pedidoDao().sent(userId) : db.pedidoDao().received(userId);
        binding.empty.emptyMessage.setText(sent ? R.string.trades_empty_sent : R.string.trades_empty_received);
        source.observe(getViewLifecycleOwner(), list -> {
            List<PedidoRow> ordered = new ArrayList<>();
            List<PedidoRow> rest = new ArrayList<>();
            if (list != null) {
                for (PedidoRow r : list) (Labels.needsAction(r.pedido, userId) ? ordered : rest).add(r);
            }
            ordered.addAll(rest);
            adapter.submitList(ordered);
            binding.empty.getRoot().setVisibility(ordered.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) outState.putBoolean(KEY_SENT, binding.toggle.getCheckedButtonId() == R.id.tab_sent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
