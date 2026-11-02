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
import br.ufms.leiaufms.core.security.PasswordHasher;
import br.ufms.leiaufms.core.util.Validators;
import br.ufms.leiaufms.databinding.FragmentChangePasswordBinding;
import br.ufms.leiaufms.ui.common.FieldErrors;

/** RF-08: exige a senha atual e grava um hash novo com salt novo. */
public class ChangePasswordFragment extends Fragment {

    private FragmentChangePasswordBinding binding;
    private AccountViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        viewModel.state().observe(getViewLifecycleOwner(), this::render);
        binding.buttonSave.setOnClickListener(v -> submit());
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        boolean dirty = binding.inputCurrent.length() > 0 || binding.inputPassword.length() > 0
                                || binding.inputConfirm.length() > 0;
                        if (!dirty) {
                            NavHostFragment.findNavController(ChangePasswordFragment.this).popBackStack();
                            return;
                        }
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle(R.string.discard_changes_title)
                                .setMessage(R.string.discard_changes_message)
                                .setNegativeButton(R.string.action_keep_editing, null)
                                .setPositiveButton(R.string.action_discard, (d, w) ->
                                        NavHostFragment.findNavController(ChangePasswordFragment.this).popBackStack())
                                .show();
                    }
                });
    }

    private void submit() {
        char[] atual = FieldErrors.chars(binding.inputCurrent);
        char[] nova = FieldErrors.chars(binding.inputPassword);
        char[] confirmacao = FieldErrors.chars(binding.inputConfirm);
        TextInputLayout first = null;
        if (atual.length == 0) {
            binding.layoutCurrent.setError(getString(R.string.error_empty_password));
            first = binding.layoutCurrent;
        } else {
            binding.layoutCurrent.setError(null);
        }
        Validators.Result r = Validators.password(nova);
        boolean ok = r == Validators.Result.TOO_LONG
                ? setError(binding.layoutPassword, getString(R.string.error_password_long))
                : FieldErrors.show(binding.layoutPassword, r, R.string.error_empty_password, AppConfig.PASSWORD_MAX);
        if (!ok && first == null) first = binding.layoutPassword;
        if (!FieldErrors.show(binding.layoutConfirm, Validators.confirmation(nova, confirmacao),
                R.string.error_empty_confirmation, AppConfig.PASSWORD_MAX) && first == null)
            first = binding.layoutConfirm;
        PasswordHasher.wipe(confirmacao);
        if (first != null) {
            PasswordHasher.wipe(atual);
            PasswordHasher.wipe(nova);
            if (first.getEditText() != null) first.getEditText().requestFocus();
            return;
        }
        viewModel.changePassword(atual, nova);
    }

    private boolean setError(TextInputLayout layout, String message) {
        layout.setError(message);
        return false;
    }

    private void render(AccountViewModel.State state) {
        boolean loading = state == AccountViewModel.State.LOADING;
        binding.buttonSave.setEnabled(!loading);
        binding.buttonSave.setText(loading ? R.string.action_changing : R.string.action_change_password);
        binding.progress.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
        switch (state) {
            case SAVED:
                viewModel.consume();
                Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.password_changed,
                        Snackbar.LENGTH_SHORT).setAnchorView(requireActivity().findViewById(R.id.bottom_nav)).show();
                NavHostFragment.findNavController(this).popBackStack();
                break;
            case WRONG_CURRENT:
                binding.layoutCurrent.setError(getString(R.string.error_current_password));
                binding.inputCurrent.setText(null);
                binding.inputCurrent.requestFocus();
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
