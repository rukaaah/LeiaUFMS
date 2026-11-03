package br.ufms.leiaufms.ui.feed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import br.ufms.leiaufms.AppConfig;
import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.core.util.Validators;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.Obra;
import br.ufms.leiaufms.data.local.entity.PostMural;
import br.ufms.leiaufms.data.repository.SocialRepository;
import br.ufms.leiaufms.databinding.FragmentNewPostBinding;
import br.ufms.leiaufms.ui.common.FieldErrors;

/** Novo post (RF-38): tipo, livro obrigatório e texto até 500 caracteres. */
public class NewPostFragment extends Fragment {

    public static final String ARG_TYPE = "tipo";
    public static final String ARG_TITLE = "titulo";
    public static final String ARG_AUTHOR = "autor";

    private FragmentNewPostBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNewPostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle a = getArguments();
        if (a != null && savedInstanceState == null) {
            String tipo = a.getString(ARG_TYPE);
            if (PostMural.TIPO_TROCA_CONCLUIDA.equals(tipo)) binding.types.check(R.id.t_troca);
            else if (PostMural.TIPO_DOACAO.equals(tipo)) binding.types.check(R.id.t_doacao);
            binding.inputTitle.setText(a.getString(ARG_TITLE));
            binding.inputAuthor.setText(a.getString(ARG_AUTHOR));
        }
        br.ufms.leiaufms.ui.common.BookSuggestions.attach(binding.inputTitle, binding.inputAuthor);
        binding.buttonPublish.setOnClickListener(v -> submit());
    }

    private void submit() {
        int t = binding.types.getCheckedChipId();
        String titulo = FieldErrors.text(binding.inputTitle);
        String autor = FieldErrors.text(binding.inputAuthor);
        String texto = FieldErrors.text(binding.inputText);
        boolean ok = true;
        binding.errorType.setText(R.string.error_choose_post_type);
        binding.errorType.setVisibility(t == View.NO_ID ? View.VISIBLE : View.GONE);
        if (t == View.NO_ID) ok = false;
        ok &= FieldErrors.show(binding.layoutTitle, Validators.required(titulo, AppConfig.TITLE_MAX),
                R.string.error_post_book, AppConfig.TITLE_MAX);
        ok &= FieldErrors.show(binding.layoutAuthor, Validators.required(autor, AppConfig.AUTHOR_MAX),
                R.string.error_empty_author, AppConfig.AUTHOR_MAX);
        ok &= FieldErrors.show(binding.layoutText, Validators.required(texto, AppConfig.POST_MAX_CHARS),
                R.string.error_post_text, AppConfig.POST_MAX_CHARS);
        if (!ok) return;
        String tipo = t == R.id.t_procuro ? PostMural.TIPO_PROCURO : t == R.id.t_doacao ? PostMural.TIPO_DOACAO
                : t == R.id.t_troca ? PostMural.TIPO_TROCA_CONCLUIDA : PostMural.TIPO_DICA;
        long me = SessionManager.get(requireContext()).currentUserId();
        android.content.Context app = requireContext().getApplicationContext();
        binding.buttonPublish.setEnabled(false);
        AppExecutors.io().execute(() -> {
            SocialRepository.Result r = new SocialRepository(app).post(me, tipo, titulo, autor, texto, null);
            AppExecutors.main(() -> {
                if (binding == null) return;
                binding.buttonPublish.setEnabled(true);
                if (r == SocialRepository.Result.OK) {
                    Snackbar.make(requireActivity().findViewById(android.R.id.content), R.string.post_published,
                            Snackbar.LENGTH_SHORT).setAnchorView(requireActivity().findViewById(R.id.bottom_nav)).show();
                    NavHostFragment.findNavController(this).popBackStack();
                } else {
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
