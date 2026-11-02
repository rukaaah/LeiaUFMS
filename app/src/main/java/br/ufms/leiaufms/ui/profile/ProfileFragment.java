package br.ufms.leiaufms.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.databinding.FragmentProfileBasicBinding;
import br.ufms.leiaufms.ui.main.MainActivity;

/** RF-06 (versão inicial) e RF-05: dados do aluno e "Sair" com confirmação. */
public class ProfileFragment extends Fragment {

    private FragmentProfileBasicBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBasicBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        long userId = SessionManager.get(requireContext()).currentUserId();
        AppDatabase.get(requireContext()).usuarioDao().observe(userId).observe(getViewLifecycleOwner(), u -> {
            if (u == null) return;
            binding.textName.setText(u.nome);
            binding.textCourse.setText(getString(R.string.profile_course_semester, u.curso, u.semestre));
            binding.textEmail.setText(u.email);
        });
        binding.buttonLogout.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_logout, (d, w) -> ((MainActivity) requireActivity()).logout())
                .show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
