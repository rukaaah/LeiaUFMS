package br.ufms.leiaufms.ui.shelf;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import br.ufms.leiaufms.AppConfig;
import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.core.util.Validators;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.ItemEstante;
import br.ufms.leiaufms.data.local.entity.Obra;
import br.ufms.leiaufms.data.local.model.ShelfRow;
import br.ufms.leiaufms.data.repository.SocialRepository;
import br.ufms.leiaufms.databinding.DialogAddBookBinding;
import br.ufms.leiaufms.databinding.FragmentShelfBinding;
import br.ufms.leiaufms.ui.common.FieldErrors;
import br.ufms.leiaufms.ui.listing.ListingDetailFragment;
import br.ufms.leiaufms.ui.listing.NewListingFragment;

/**
 * Estante (RF-35, RF-36) e lista de desejos (RF-37). Ao marcar "Lido", pergunta se quer passar o
 * livro adiante e abre o anúncio pré-preenchido.
 */
public class ShelfFragment extends Fragment {


    private FragmentShelfBinding binding;
    private LiveData<List<ShelfRow>> source;
    private ShelfAdapter adapter;
    private long me;
    private Context app;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentShelfBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        me = SessionManager.get(requireContext()).currentUserId();
        app = requireContext().getApplicationContext();
        adapter = new ShelfAdapter(this::onItem);
        binding.list.setAdapter(adapter);
        // Desejos têm aba própria na barra; aqui fica só a estante (feedback 03/11: sem repetição)
        binding.tabs.setVisibility(View.GONE);
        binding.fabAdd.setOnClickListener(v -> askBook());
        observe();
    }

    private boolean wishes() {
        return false;
    }

    private void observe() {
        if (source != null) source.removeObservers(getViewLifecycleOwner());
        AppDatabase db = AppDatabase.get(requireContext());
        boolean w = wishes();
        source = w ? db.socialDao().wishes(me) : db.socialDao().shelf(me);
        binding.empty.emptyIcon.setImageResource(w ? R.drawable.ic_bookmark_border : R.drawable.ic_library);
        binding.empty.emptyTitle.setText(w ? R.string.wishes_empty_title : R.string.shelf_empty_title);
        binding.empty.emptyMessage.setText(w ? R.string.wishes_empty_message : R.string.shelf_empty_message);
        source.observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list == null ? new ArrayList<>() : list);
            binding.empty.getRoot().setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void onItem(ShelfRow r) {
        List<String> labels = new ArrayList<>();
        List<Runnable> actions = new ArrayList<>();
        if ("DESEJO".equals(r.status)) {
            if (r.anuncioId != null) {
                labels.add(getString(R.string.action_view_listing));
                actions.add(() -> {
                    Bundle args = new Bundle();
                    args.putLong(ListingDetailFragment.ARG_ID, r.anuncioId);
                    NavHostFragment.findNavController(this).navigate(R.id.listingDetailFragment, args);
                });
            }
            labels.add(getString(R.string.action_remove));
            actions.add(() -> AppExecutors.io().execute(() -> new SocialRepository(app).removeWish(r.id, me)));
        } else {
            if (ItemEstante.STATUS_LENDO.equals(r.status)) {
                labels.add(getString(R.string.action_mark_read));
                actions.add(() -> markRead(r));
            } else {
                labels.add(getString(R.string.action_mark_reading));
                actions.add(() -> setStatus(r, ItemEstante.STATUS_LENDO));
                labels.add(getString(R.string.action_pass_on));
                actions.add(() -> announce(r));
            }
            labels.add(getString(R.string.action_remove));
            actions.add(() -> AppExecutors.io().execute(() -> new SocialRepository(app).removeShelf(r.id, me)));
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(r.titulo)
                .setItems(labels.toArray(new String[0]), (d, which) -> actions.get(which).run())
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    /** RF-36: "Terminei de ler" pergunta se quer passar o livro adiante. */
    private void markRead(ShelfRow r) {
        setStatus(r, ItemEstante.STATUS_LIDO);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.pass_on_title)
                .setMessage(getString(R.string.pass_on_message, r.titulo))
                .setNegativeButton(R.string.action_not_now, null)
                .setPositiveButton(R.string.action_announce, (d, w) -> announce(r))
                .show();
    }

    private void announce(ShelfRow r) {
        Bundle args = new Bundle();
        args.putString(NewListingFragment.ARG_TITLE, r.titulo);
        args.putString(NewListingFragment.ARG_AUTHOR, r.autor);
        NavHostFragment.findNavController(this).navigate(R.id.newListingFragment, args);
    }

    private void setStatus(ShelfRow r, String status) {
        AppExecutors.io().execute(() -> new SocialRepository(app).setShelfStatus(r.id, me, status));
    }

    private void askBook() {
        AddBookDialog.show(this, wishes(), binding.getRoot());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
