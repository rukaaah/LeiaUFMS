package br.ufms.leiaufms.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import br.ufms.leiaufms.AppConfig;
import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.util.Validators;
import br.ufms.leiaufms.data.local.entity.Usuario;
import br.ufms.leiaufms.databinding.FragmentEditProfileBinding;
import br.ufms.leiaufms.ui.common.FieldErrors;

/** RF-07: editar nome, e-mail, curso, semestre e status, com as mesmas regras do cadastro. */
public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private AccountViewModel viewModel;
    private Usuario original;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        br.ufms.leiaufms.ui.common.Courses.attach(binding.inputCourse);
        viewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        viewModel.user().observe(getViewLifecycleOwner(), u -> {
            if (u == null || original != null) return;
            original = u;
            if (savedInstanceState == null) {
                binding.inputName.setText(u.nome);
                binding.inputEmail.setText(u.email);
                binding.inputCourse.setText(u.curso, false);
                binding.inputSemester.setText(String.valueOf(u.semestre));
                binding.inputStatus.setText(u.status);
            }
        });
        viewModel.load();
        viewModel.state().observe(getViewLifecycleOwner(), this::render);
        binding.buttonSave.setOnClickListener(v -> submit());
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        leave(this);
                    }
                });
    }

    private boolean dirty() {
        if (original == null) return false;
        String status = original.status == null ? "" : original.status;
        return !FieldErrors.text(binding.inputName).trim().equals(original.nome)
                || !Validators.normalizeEmail(FieldErrors.text(binding.inputEmail)).equals(original.email)
                || !FieldErrors.text(binding.inputCourse).trim().equals(original.curso)
                || !FieldErrors.text(binding.inputSemester).trim().equals(String.valueOf(original.semestre))
                || !FieldErrors.text(binding.inputStatus).trim().equals(status);
    }

    private void leave(OnBackPressedCallback callback) {
        if (!dirty()) {
            NavHostFragment.findNavController(this).popBackStack();
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.discard_changes_title)
                .setMessage(R.string.discard_changes_message)
                .setNegativeButton(R.string.action_keep_editing, null)
                .setPositiveButton(R.string.action_discard, (d, w) -> NavHostFragment.findNavController(this).popBackStack())
                .show();
    }

    private void submit() {
        String nome = FieldErrors.text(binding.inputName);
        String email = FieldErrors.text(binding.inputEmail);
        String curso = FieldErrors.text(binding.inputCourse);
        String semestre = FieldErrors.text(binding.inputSemester);
        String status = FieldErrors.text(binding.inputStatus);
        TextInputLayout first = null;
        if (!FieldErrors.show(binding.layoutName, Validators.name(nome), R.string.error_empty_name, AppConfig.NAME_MAX))
            first = binding.layoutName;
        if (!FieldErrors.show(binding.layoutEmail, Validators.email(email), R.string.error_empty_email, AppConfig.EMAIL_MAX)
                && first == null) first = binding.layoutEmail;
        if (!FieldErrors.show(binding.layoutCourse, Validators.required(curso, AppConfig.COURSE_MAX),
                R.string.error_empty_course, AppConfig.COURSE_MAX)) {
            if (first == null) first = binding.layoutCourse;
        } else if (br.ufms.leiaufms.ui.common.Courses.official(requireContext(), curso) != null) {
            curso = br.ufms.leiaufms.ui.common.Courses.official(requireContext(), curso);
        } else if (original == null || !curso.trim().equals(original.curso)) {
            // Curso antigo fora da lista continua valendo até a pessoa mudar
            binding.layoutCourse.setError(getString(R.string.error_course_list));
            if (first == null) first = binding.layoutCourse;
        }
        if (!FieldErrors.show(binding.layoutSemester, Validators.semester(semestre), R.string.error_empty_semester, 2)
                && first == null) first = binding.layoutSemester;
        if (!FieldErrors.show(binding.layoutStatus, Validators.optional(status, AppConfig.STATUS_MAX),
                R.string.error_generic, AppConfig.STATUS_MAX) && first == null) first = binding.layoutStatus;
        if (first != null) {
            if (first.getEditText() != null) first.getEditText().requestFocus();
            return;
        }
        viewModel.saveProfile(nome, email, curso, Integer.parseInt(semestre.trim()), status);
    }

    private void render(AccountViewModel.State state) {
        boolean loading = state == AccountViewModel.State.LOADING;
        binding.buttonSave.setEnabled(!loading);
        binding.buttonSave.setText(loading ? R.string.action_saving : R.string.action_save);
        binding.progress.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
        switch (state) {
            case SAVED:
                viewModel.consume();
                View anchor = requireActivity().findViewById(R.id.bottom_nav);
                Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.profile_updated,
                        Snackbar.LENGTH_SHORT).setAnchorView(anchor).show();
                NavHostFragment.findNavController(this).popBackStack();
                break;
            case EMAIL_TAKEN:
                binding.layoutEmail.setError(getString(R.string.error_email_taken));
                binding.inputEmail.requestFocus();
                viewModel.consume();
                break;
            case ERROR:
                Snackbar.make(binding.getRoot(), R.string.error_generic, Snackbar.LENGTH_LONG).show();
                viewModel.consume();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
