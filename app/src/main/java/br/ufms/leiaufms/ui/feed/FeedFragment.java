package br.ufms.leiaufms.ui.feed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import br.ufms.leiaufms.AppConfig;
import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.PostMural;
import br.ufms.leiaufms.data.local.entity.Reacao;
import br.ufms.leiaufms.data.local.model.PostRow;
import br.ufms.leiaufms.data.repository.SocialRepository;
import br.ufms.leiaufms.databinding.FragmentFeedBinding;
import br.ufms.leiaufms.ui.listing.ListingDetailFragment;

/**
 * Mural (RF-38, RF-39, PC-11), estrutura Pinterest: finito (14 dias, 30 itens), filtros por tipo,
 * reações "Útil" e "Quero" que alternam. "Quero" num post de Doação leva ao anúncio.
 */
public class FeedFragment extends Fragment {

    private static final String FILTER_SAVED = "SALVOS";

    private FragmentFeedBinding binding;
    private LiveData<List<PostRow>> source;
    private PostAdapter adapter;
    private long me;
    private boolean moderator;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        me = SessionManager.get(requireContext()).currentUserId();
        android.content.Context app = requireContext().getApplicationContext();
        AppExecutors.io().execute(() -> {
            br.ufms.leiaufms.data.local.entity.Usuario u = AppDatabase.get(app).usuarioDao().findById(me);
            moderator = u != null && u.isModerador();
        });
        adapter = new PostAdapter(new PostAdapter.Listener() {
            @Override
            public void onReact(PostRow p, String tipo) {
                AppExecutors.io().execute(() -> new SocialRepository(app).toggleReaction(p.id, me, tipo));
            }

            @Override
            public void onOpen(PostRow p) {
                Bundle args = new Bundle();
                args.putLong(PostFragment.ARG_ID, p.id);
                NavHostFragment.findNavController(FeedFragment.this).navigate(R.id.postFragment, args);
            }

            @Override
            public void onLongPress(PostRow p) {
                if (!moderator) return;
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.action_hide_post)
                        .setMessage(p.texto)
                        .setNegativeButton(R.string.action_cancel, null)
                        .setPositiveButton(R.string.action_hide_post, (d, w) -> AppExecutors.io().execute(() -> {
                            new SocialRepository(app).hidePost(me, p.id);
                            AppExecutors.main(() -> {
                                if (binding != null) Snackbar.make(binding.getRoot(), R.string.post_hidden, Snackbar.LENGTH_SHORT).show();
                            });
                        }))
                        .show();
            }
        });
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        binding.list.setLayoutManager(lm);
        binding.list.setAdapter(adapter);
        binding.empty.emptyIcon.setImageResource(R.drawable.ic_forum);
        binding.empty.emptyTitle.setText(R.string.empty_feed_title);
        binding.empty.emptyMessage.setText(R.string.empty_feed_message);
        binding.empty.emptyAction.setVisibility(View.VISIBLE);
        binding.empty.emptyAction.setText(R.string.action_new_post);
        binding.empty.emptyAction.setOnClickListener(v -> newPost());
        binding.filters.setOnCheckedStateChangeListener((g, ids) -> observe());
        binding.fabPost.setOnClickListener(v -> newPost());
        observe();
    }

    private void observe() {
        if (source != null) source.removeObservers(getViewLifecycleOwner());
        int id = binding.filters.getCheckedChipId();
        String tipo = id == R.id.f_dica ? PostMural.TIPO_DICA : id == R.id.f_procuro ? PostMural.TIPO_PROCURO
                : id == R.id.f_doacao ? PostMural.TIPO_DOACAO : id == R.id.f_troca ? PostMural.TIPO_TROCA_CONCLUIDA
                : id == R.id.f_saved ? FILTER_SAVED : "";
        binding.empty.emptyMessage.setText(FILTER_SAVED.equals(tipo) ? R.string.feed_saved_empty : R.string.empty_feed_message);
        source = AppDatabase.get(requireContext()).socialDao().feed(me, System.currentTimeMillis(), tipo,
                AppConfig.FEED_MAX_ITEMS);
        source.observe(getViewLifecycleOwner(), list -> {
            adapter.submit(list);
            binding.empty.getRoot().setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void newPost() {
        NavHostFragment.findNavController(this).navigate(R.id.newPostFragment);
    }

    private void openListing(long anuncioId) {
        Bundle args = new Bundle();
        args.putLong(ListingDetailFragment.ARG_ID, anuncioId);
        NavHostFragment.findNavController(this).navigate(R.id.listingDetailFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
