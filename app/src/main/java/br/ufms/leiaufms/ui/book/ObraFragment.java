package br.ufms.leiaufms.ui.book;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.Disciplina;
import br.ufms.leiaufms.data.local.entity.EdicaoCompativel;
import br.ufms.leiaufms.data.local.entity.Obra;
import br.ufms.leiaufms.data.local.entity.Usuario;
import br.ufms.leiaufms.data.repository.SocialRepository;
import br.ufms.leiaufms.databinding.FragmentObraBinding;
import br.ufms.leiaufms.ui.common.Covers;
import br.ufms.leiaufms.ui.listing.ListingAdapter;
import br.ufms.leiaufms.ui.listing.ListingDetailFragment;

/**
 * Hub da obra (RF-15): todas as cópias anunciadas, edições compatíveis (o Moderador marca) e
 * disciplinas que indicam o livro.
 */
public class ObraFragment extends Fragment {

    public static final String ARG_ID = "obraId";

    private FragmentObraBinding binding;
    private long obraId;
    private long me;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentObraBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        obraId = requireArguments().getLong(ARG_ID);
        me = SessionManager.get(requireContext()).currentUserId();
        ListingAdapter adapter = new ListingAdapter(row -> {
            Bundle args = new Bundle();
            args.putLong(ListingDetailFragment.ARG_ID, row.anuncioId);
            NavHostFragment.findNavController(this).navigate(R.id.listingDetailFragment, args);
        });
        binding.copies.setAdapter(adapter);
        AppDatabase.get(requireContext()).anuncioDao().byObra(obraId).observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            binding.noCopies.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });
        binding.buttonWish.setOnClickListener(v -> {
            android.content.Context app = requireContext().getApplicationContext();
            AppExecutors.io().execute(() -> {
                new SocialRepository(app).toggleWish(me, obraId);
                load();
            });
        });
        binding.buttonCompatible.setOnClickListener(v -> markCompatible());
        load();
    }

    private void load() {
        android.content.Context app = requireContext().getApplicationContext();
        AppExecutors.io().execute(() -> {
            AppDatabase db = AppDatabase.get(app);
            Obra o = db.obraDao().findById(obraId);
            List<Obra> compat = db.obraDao().compatibles(obraId);
            List<Disciplina> discs = db.obraDao().disciplinaRows(obraId);
            boolean wished = db.socialDao().hasWish(me, obraId) > 0;
            int wishCount = db.socialDao().wishCount(obraId);
            Usuario u = db.usuarioDao().findById(me);
            AppExecutors.main(() -> {
                if (binding == null || o == null) return;
                binding.title.setText(o.titulo);
                binding.author.setText(o.autor);
                Covers.bind(binding.cover.coverImage, binding.cover.coverText, o.capaPath, o.titulo);
                binding.wishes.setText(getResources().getQuantityString(R.plurals.wish_count_total, wishCount, wishCount));
                binding.buttonWish.setText(wished ? R.string.wish_remove : R.string.wish_add);
                binding.buttonWish.setIconResource(wished ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
                binding.compatible.removeAllViews();
                binding.compatibleTitle.setVisibility(compat.isEmpty() && (u == null || !u.isModerador())
                        ? View.GONE : View.VISIBLE);
                for (Obra c : compat) {
                    com.google.android.material.button.MaterialButton b = new com.google.android.material.button.MaterialButton(
                            requireContext(), null, androidx.appcompat.R.attr.borderlessButtonStyle);
                    b.setText(c.titulo + " · " + c.autor);
                    b.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
                    b.setOnClickListener(v -> openObra(c.id));
                    binding.compatible.addView(b);
                }
                binding.buttonCompatible.setVisibility(u != null && u.isModerador() ? View.VISIBLE : View.GONE);
                binding.disciplines.removeAllViews();
                binding.disciplinesTitle.setVisibility(discs.isEmpty() ? View.GONE : View.VISIBLE);
                for (Disciplina d : discs) {
                    Chip chip = new Chip(requireContext());
                    chip.setText(d.nome);
                    chip.setChipIconResource(R.drawable.ic_school);
                    chip.setChipIconVisible(true);
                    chip.setOnClickListener(v -> {
                        Bundle args = new Bundle();
                        args.putLong(DisciplinaFragment.ARG_ID, d.id);
                        NavHostFragment.findNavController(this).navigate(R.id.disciplinaFragment, args);
                    });
                    binding.disciplines.addView(chip);
                }
            });
        });
    }

    /** Moderador marca outra obra como edição compatível desta. */
    private void markCompatible() {
        android.content.Context app = requireContext().getApplicationContext();
        AppExecutors.io().execute(() -> {
            List<Obra> all = AppDatabase.get(app).obraDao().all();
            all.removeIf(o -> o.id == obraId);
            AppExecutors.main(() -> {
                if (binding == null) return;
                String[] items = new String[all.size()];
                for (int i = 0; i < all.size(); i++) items[i] = all.get(i).titulo + " · " + all.get(i).autor;
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.action_mark_compatible)
                        .setItems(items, (d, which) -> AppExecutors.io().execute(() -> {
                            EdicaoCompativel e = new EdicaoCompativel();
                            e.obraId = obraId;
                            e.obraCompativelId = all.get(which).id;
                            AppDatabase.get(app).obraDao().insertCompativel(e);
                            AppExecutors.main(() -> {
                                if (binding == null) return;
                                Snackbar.make(binding.getRoot(), R.string.compatible_saved, Snackbar.LENGTH_SHORT).show();
                                load();
                            });
                        }))
                        .setNegativeButton(R.string.action_cancel, null)
                        .show();
            });
        });
    }

    private void openObra(long id) {
        Bundle args = new Bundle();
        args.putLong(ARG_ID, id);
        NavHostFragment.findNavController(this).navigate(R.id.obraFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
