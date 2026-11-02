package br.ufms.leiaufms.ui.trade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.databinding.FragmentPlaceholderBinding;

/** Aba da navegação principal. Conteúdo real entra nas próximas partes. */
public class TradesFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentPlaceholderBinding binding = FragmentPlaceholderBinding.inflate(inflater, container, false);
        binding.empty.emptyIcon.setImageResource(R.drawable.ic_swap);
        binding.empty.emptyTitle.setText(R.string.empty_trades_title);
        binding.empty.emptyMessage.setText(R.string.empty_trades_message);
        return binding.getRoot();
    }
}
