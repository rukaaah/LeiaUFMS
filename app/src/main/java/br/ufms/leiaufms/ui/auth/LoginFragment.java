package br.ufms.leiaufms.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import br.ufms.leiaufms.AppConfig;
import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.security.PasswordHasher;
import br.ufms.leiaufms.core.util.Validators;
import br.ufms.leiaufms.databinding.FragmentLoginBinding;
import br.ufms.leiaufms.ui.common.FieldErrors;

/** RF-03: login com e-mail institucional e senha. Mensagem única para credencial errada. */
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        binding.buttonLogin.setOnClickListener(v -> submit());
        binding.inputPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });
        binding.buttonGoRegister.setOnClickListener(v -> ((AuthActivity) requireActivity()).showRegister());
        viewModel.state().observe(getViewLifecycleOwner(), this::render);
    }

    private void submit() {
        String email = FieldErrors.text(binding.inputEmail);
        Validators.Result emailResult = Validators.email(email);
        // No login só checamos vazio e formato; domínio errado cai na mensagem única de credencial
        boolean ok = FieldErrors.show(binding.layoutEmail,
                emailResult == Validators.Result.EMPTY ? emailResult : Validators.Result.OK,
                R.string.error_empty_email, AppConfig.EMAIL_MAX);
        char[] senha = FieldErrors.chars(binding.inputPassword);
        if (senha.length == 0) {
            binding.layoutPassword.setError(getString(R.string.error_empty_password));
            ok = false;
        } else {
            binding.layoutPassword.setError(null);
        }
        if (!ok) {
            PasswordHasher.wipe(senha);
            (binding.layoutEmail.getError() != null ? binding.inputEmail : binding.inputPassword).requestFocus();
            return;
        }
        viewModel.login(email, senha);
    }

    private void render(AuthViewModel.State state) {
        boolean loading = state == AuthViewModel.State.LOADING;
        binding.buttonLogin.setEnabled(!loading);
        binding.buttonLogin.setText(loading ? R.string.action_login_loading : R.string.action_login);
        binding.inputEmail.setEnabled(!loading);
        binding.inputPassword.setEnabled(!loading);
        binding.progress.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
        switch (state) {
            case SUCCESS:
                ((AuthActivity) requireActivity()).openMain();
                break;
            case INVALID_CREDENTIALS:
                binding.layoutPassword.setError(getString(R.string.error_login_failed));
                binding.inputPassword.setText(null);
                binding.inputPassword.requestFocus();
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
