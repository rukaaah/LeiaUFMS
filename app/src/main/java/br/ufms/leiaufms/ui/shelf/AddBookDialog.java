package br.ufms.leiaufms.ui.shelf;

import android.content.Context;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import br.ufms.leiaufms.AppConfig;
import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.core.util.Validators;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.ItemEstante;
import br.ufms.leiaufms.data.local.entity.Obra;
import br.ufms.leiaufms.data.repository.SocialRepository;
import br.ufms.leiaufms.databinding.DialogAddBookBinding;
import br.ufms.leiaufms.ui.common.FieldErrors;

/**
 * Diálogo "Adicionar livro" da estante ou dos desejos. Adiciona ali mesmo, sem trocar de tela
 * (feedback 03/11: depois de adicionar, a pessoa fica onde tocou em adicionar).
 */
public final class AddBookDialog {

    private AddBookDialog() {
    }

    public static void show(Fragment f, boolean wish, View snackbarAnchor) {
        Context app = f.requireContext().getApplicationContext();
        long me = SessionManager.get(app).currentUserId();
        DialogAddBookBinding d = DialogAddBookBinding.inflate(f.getLayoutInflater());
        // Sugestões de obras já cadastradas (título → autor preenchido)
        AppExecutors.io().execute(() -> {
            List<Obra> obras = AppDatabase.get(app).obraDao().all();
            AppExecutors.main(() -> {
                String[] titles = new String[obras.size()];
                for (int i = 0; i < obras.size(); i++) titles[i] = obras.get(i).titulo;
                d.inputTitle.setSimpleItems(titles);
                d.inputTitle.setOnItemClickListener((parent, v, pos, id) -> {
                    String chosen = (String) parent.getItemAtPosition(pos);
                    for (Obra o : obras) if (o.titulo.equals(chosen)) d.inputAuthor.setText(o.autor);
                });
            });
        });
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(f.requireContext())
                .setTitle(wish ? R.string.add_wish_title : R.string.add_shelf_title)
                .setView(d.getRoot())
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_add_book, null)
                .create();
        dialog.setOnShowListener(x -> dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String titulo = FieldErrors.text(d.inputTitle);
                    String autor = FieldErrors.text(d.inputAuthor);
                    boolean ok = FieldErrors.show(d.layoutTitle, Validators.required(titulo, AppConfig.TITLE_MAX),
                            R.string.error_empty_title, AppConfig.TITLE_MAX);
                    ok &= FieldErrors.show(d.layoutAuthor, Validators.required(autor, AppConfig.AUTHOR_MAX),
                            R.string.error_empty_author, AppConfig.AUTHOR_MAX);
                    if (!ok) return;
                    dialog.dismiss();
                    AppExecutors.io().execute(() -> {
                        SocialRepository repo = new SocialRepository(app);
                        SocialRepository.Result r = SocialRepository.Result.OK;
                        if (wish) r = repo.addWish(me, titulo, autor);
                        else repo.addToShelf(me, titulo, autor, ItemEstante.STATUS_LENDO);
                        SocialRepository.Result result = r;
                        AppExecutors.main(() -> {
                            if (f.getView() == null) return;
                            int msg = result == SocialRepository.Result.DUPLICATE ? R.string.wish_duplicate
                                    : wish ? R.string.wish_added : R.string.shelf_added;
                            Snackbar.make(snackbarAnchor, msg, Snackbar.LENGTH_LONG).show();
                        });
                    });
                }));
        dialog.show();
    }
}
