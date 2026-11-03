package br.ufms.leiaufms.ui.trade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.databinding.FragmentListBinding;

/** RF-26: lista de pedidos em disputa para o Moderador decidir. */
public class DisputesFragment extends Fragment {

    private FragmentListBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        long me = SessionManager.get(requireContext()).currentUserId();
        TradeAdapter adapter = new TradeAdapter(me, row -> {
            Bundle args = new Bundle();
            args.putLong(TradeDetailFragment.ARG_ID, row.pedido.id);
            NavHostFragment.findNavController(this).navigate(R.id.tradeDetailFragment, args);
        });
        binding.list.setAdapter(adapter);
        binding.empty.emptyIcon.setImageResource(R.drawable.ic_gavel);
        binding.empty.emptyTitle.setText(R.string.title_disputes);
        binding.empty.emptyMessage.setText(R.string.disputes_empty);
        AppDatabase.get(requireContext()).pedidoDao().disputes().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            binding.empty.getRoot().setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
