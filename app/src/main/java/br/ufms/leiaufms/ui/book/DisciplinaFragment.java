package br.ufms.leiaufms.ui.book;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.List;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.Disciplina;
import br.ufms.leiaufms.data.local.entity.Obra;
import br.ufms.leiaufms.data.local.model.ShelfRow;
import br.ufms.leiaufms.databinding.FragmentListBinding;
import br.ufms.leiaufms.ui.shelf.ShelfAdapter;

/** Hub da disciplina (RF-16): livros indicados e quantas cópias estão disponíveis. */
public class DisciplinaFragment extends Fragment {

    public static final String ARG_ID = "disciplinaId";

    private FragmentListBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        long id = requireArguments().getLong(ARG_ID);
        long me = SessionManager.get(requireContext()).currentUserId();
        ShelfAdapter adapter = new ShelfAdapter(row -> {
            Bundle args = new Bundle();
            args.putLong(ObraFragment.ARG_ID, row.obraId);
            NavHostFragment.findNavController(this).navigate(R.id.obraFragment, args);
        });
        binding.list.setAdapter(adapter);
        android.content.Context app = requireContext().getApplicationContext();
        AppExecutors.io().execute(() -> {
            AppDatabase db = AppDatabase.get(app);
            Disciplina d = db.obraDao().disciplina(id);
            List<ShelfRow> rows = new ArrayList<>();
            for (Obra o : db.obraDao().obrasOf(id)) {
                ShelfRow r = new ShelfRow();
                r.id = o.id;
                r.obraId = o.id;
                r.titulo = o.titulo;
                r.autor = o.autor;
                r.capaPath = o.capaPath;
                r.status = "DESEJO";
                r.disponiveis = db.anuncioDao().activeCountForObra(o.id, me);
                rows.add(r);
            }
            AppExecutors.main(() -> {
                if (binding == null) return;
                if (d != null) {
                    com.google.android.material.appbar.MaterialToolbar t = requireActivity().findViewById(R.id.toolbar);
                    if (t != null) t.setTitle(d.nome);
                }
                adapter.submitList(rows);
                binding.empty.getRoot().setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
