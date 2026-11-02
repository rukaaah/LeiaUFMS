package br.ufms.leiaufms.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import br.ufms.leiaufms.AppConfig;
import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.security.PasswordHasher;
import br.ufms.leiaufms.core.util.Validators;
import br.ufms.leiaufms.databinding.FragmentRegisterBinding;
import br.ufms.leiaufms.ui.common.FieldErrors;

/** RF-02: cadastro com validação por campo (RN-AUTH-01 a 04) e hash BCrypt. */
public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        binding.toolbar.setNavigationOnClickListener(v -> leave());
        binding.buttonGoLogin.setOnClickListener(v -> leave());
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        leave();
                    }
                });
        binding.buttonRegister.setOnClickListener(v -> submit());
        binding.inputConfirm.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });
        viewModel.state().observe(getViewLifecycleOwner(), this::render);
    }

    private void submit() {
        String nome = FieldErrors.text(binding.inputName);
        String email = FieldErrors.text(binding.inputEmail);
        String curso = FieldErrors.text(binding.inputCourse);
        String semestre = FieldErrors.text(binding.inputSemester);
        char[] senha = FieldErrors.chars(binding.inputPassword);
        char[] confirmacao = FieldErrors.chars(binding.inputConfirm);

        TextInputLayout firstError = null;
        if (!FieldErrors.show(binding.layoutName, Validators.name(nome), R.string.error_empty_name, AppConfig.NAME_MAX))
            firstError = binding.layoutName;
        if (!FieldErrors.show(binding.layoutEmail, Validators.email(email), R.string.error_empty_email, AppConfig.EMAIL_MAX)
                && firstError == null) firstError = binding.layoutEmail;
        if (!FieldErrors.show(binding.layoutCourse, Validators.required(curso, AppConfig.COURSE_MAX),
                R.string.error_empty_course, AppConfig.COURSE_MAX) && firstError == null)
            firstError = binding.layoutCourse;
        if (!FieldErrors.show(binding.layoutSemester, Validators.semester(semestre), R.string.error_empty_semester, 2)
                && firstError == null) firstError = binding.layoutSemester;
        Validators.Result pass = Validators.password(senha);
        boolean passOk;
        if (pass == Validators.Result.TOO_LONG) {
            binding.layoutPassword.setError(getString(R.string.error_password_long));
            passOk = false;
        } else {
            passOk = FieldErrors.show(binding.layoutPassword, pass, R.string.error_empty_password, AppConfig.PASSWORD_MAX);
        }
        if (!passOk && firstError == null) firstError = binding.layoutPassword;
        if (!FieldErrors.show(binding.layoutConfirm, Validators.confirmation(senha, confirmacao),
                R.string.error_empty_confirmation, AppConfig.PASSWORD_MAX) && firstError == null)
            firstError = binding.layoutConfirm;
        PasswordHasher.wipe(confirmacao);

        if (firstError != null) {
            PasswordHasher.wipe(senha);
            focus(firstError);
            return;
        }
        viewModel.register(nome, email, curso, Integer.parseInt(semestre.trim()), senha);
    }

    /** Pede confirmação antes de descartar um formulário já preenchido. */
    private void leave() {
        boolean dirty = !FieldErrors.text(binding.inputName).trim().isEmpty()
                || !FieldErrors.text(binding.inputEmail).trim().isEmpty()
                || !FieldErrors.text(binding.inputCourse).trim().isEmpty()
                || !FieldErrors.text(binding.inputPassword).isEmpty();
        if (!dirty) {
            ((AuthActivity) requireActivity()).showLogin();
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.discard_register_title)
                .setMessage(R.string.discard_register_message)
                .setNegativeButton(R.string.action_keep_editing, null)
                .setPositiveButton(R.string.action_discard, (d, w) -> ((AuthActivity) requireActivity()).showLogin())
                .show();
    }

    private void focus(TextInputLayout layout) {
        if (layout.getEditText() != null) layout.getEditText().requestFocus();
        binding.scroll.post(() -> binding.scroll.smoothScrollTo(0, Math.max(0, layout.getTop() - layout.getHeight())));
    }

    private void render(AuthViewModel.State state) {
        boolean loading = state == AuthViewModel.State.LOADING;
        binding.buttonRegister.setEnabled(!loading);
        binding.buttonRegister.setText(loading ? R.string.action_register_loading : R.string.action_register);
        binding.progress.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
        switch (state) {
            case SUCCESS:
                ((AuthActivity) requireActivity()).openMain();
                break;
            case EMAIL_TAKEN:
                binding.layoutEmail.setError(getString(R.string.error_email_taken));
                focus(binding.layoutEmail);
                Snackbar.make(binding.getRoot(), R.string.error_email_taken, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_go_login, v -> ((AuthActivity) requireActivity()).showLogin())
                        .show();
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
