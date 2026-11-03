package br.ufms.leiaufms.ui.wishes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.model.ListingRow;
import br.ufms.leiaufms.data.local.model.ShelfRow;
import br.ufms.leiaufms.data.repository.SocialRepository;
import br.ufms.leiaufms.databinding.FragmentWishesBinding;
import br.ufms.leiaufms.ui.book.ObraFragment;
import br.ufms.leiaufms.ui.home.BookCardAdapter;
import br.ufms.leiaufms.ui.listing.ListingDetailFragment;

/**
 * Aba Desejos (RF-37, feedback 03/11): grade de capas com o que já está disponível primeiro,
 * aviso quando a obra aparece (RF-32) e sugestões do mesmo autor ou disciplina.
 */
public class WishesFragment extends Fragment {

    private FragmentWishesBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentWishesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        long me = SessionManager.get(requireContext()).currentUserId();
        android.content.Context app = requireContext().getApplicationContext();
        WishCardAdapter adapter = new WishCardAdapter(new WishCardAdapter.Listener() {
            @Override
            public void onOpen(ShelfRow row) {
                Bundle args = new Bundle();
                if (row.anuncioId != null) {
                    args.putLong(ListingDetailFragment.ARG_ID, row.anuncioId);
                    NavHostFragment.findNavController(WishesFragment.this).navigate(R.id.listingDetailFragment, args);
                } else {
                    args.putLong(ObraFragment.ARG_ID, row.obraId);
                    NavHostFragment.findNavController(WishesFragment.this).navigate(R.id.obraFragment, args);
                }
            }

            @Override
            public void onRemove(ShelfRow row) {
                AppExecutors.io().execute(() -> new SocialRepository(app).removeWish(row.id, me));
            }
        });
        binding.grid.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.grid.setAdapter(adapter);
        binding.empty.emptyIcon.setImageResource(R.drawable.ic_favorite_border);
        binding.empty.emptyTitle.setText(R.string.wishes_empty_title);
        binding.empty.emptyMessage.setText(R.string.wishes_empty_message);
        AppDatabase db = AppDatabase.get(requireContext());
        db.socialDao().wishes(me).observe(getViewLifecycleOwner(), rows -> {
            adapter.submit(rows);
            boolean empty = rows == null || rows.isEmpty();
            binding.empty.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
            int available = 0;
            if (rows != null) for (ShelfRow r : rows) if (r.disponiveis > 0) available++;
            binding.summary.setVisibility(available > 0 ? View.VISIBLE : View.GONE);
            binding.summary.setText(getResources().getQuantityString(R.plurals.wishes_available, available, available));
        });
        BookCardAdapter suggest = new BookCardAdapter(new BookCardAdapter.Listener() {
            @Override
            public void onOpen(ListingRow row) {
                Bundle args = new Bundle();
                args.putLong(ListingDetailFragment.ARG_ID, row.anuncioId);
                NavHostFragment.findNavController(WishesFragment.this).navigate(R.id.listingDetailFragment, args);
            }

            @Override
            public void onToggleWish(ListingRow row) {
                AppExecutors.io().execute(() -> new SocialRepository(app).toggleWish(me, row.obraId));
            }
        });
        binding.suggest.setAdapter(suggest);
        db.anuncioDao().wishSuggestions(me).observe(getViewLifecycleOwner(), list -> {
            suggest.submitList(list);
            boolean any = list != null && !list.isEmpty();
            binding.suggestTitle.setVisibility(any ? View.VISIBLE : View.GONE);
            binding.suggest.setVisibility(any ? View.VISIBLE : View.GONE);
        });
        binding.fabAdd.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean(br.ufms.leiaufms.ui.shelf.ShelfFragment.ARG_WISHES, true);
            args.putBoolean(br.ufms.leiaufms.ui.shelf.ShelfFragment.ARG_ADD_NOW, true);
            NavHostFragment.findNavController(this).navigate(R.id.shelfFragment, args);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
