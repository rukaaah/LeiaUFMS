package br.ufms.leiaufms.ui.explore;

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
public class ExploreFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentPlaceholderBinding binding = FragmentPlaceholderBinding.inflate(inflater, container, false);
        binding.empty.emptyIcon.setImageResource(R.drawable.ic_explore);
        binding.empty.emptyTitle.setText(R.string.empty_explore_title);
        binding.empty.emptyMessage.setText(R.string.empty_explore_message);
        return binding.getRoot();
    }
}
