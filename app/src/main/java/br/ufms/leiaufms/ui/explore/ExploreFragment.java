package br.ufms.leiaufms.ui.explore;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.List;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.location.LocationProvider;
import br.ufms.leiaufms.core.permission.PermissionHelper;
import br.ufms.leiaufms.data.local.entity.Anuncio;
import br.ufms.leiaufms.data.local.model.ListingRow;
import br.ufms.leiaufms.databinding.FragmentExploreBinding;
import br.ufms.leiaufms.ui.listing.ListingDetailFragment;

/**
 * Explorar (RF-14): busca, filtro de modalidade e "Perto de mim" com raio (RF-27).
 * A localização é pedida só no toque em "Perto de mim" (RNF-07).
 */
public class ExploreFragment extends Fragment {

    public static final String ARG_GENRE = "genero";

    private FragmentExploreBinding binding;
    private ExploreViewModel viewModel;
    private PermissionHelper permissions;
    private LocationProvider locationProvider;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissions = new PermissionHelper(this);
        locationProvider = new LocationProvider(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ExploreViewModel.class);
        // "Todos os livros" em grade de capas (feedback 03/11), com o mesmo card do Início
        long me = br.ufms.leiaufms.core.security.SessionManager.get(requireContext()).currentUserId();
        android.content.Context app = requireContext().getApplicationContext();
        br.ufms.leiaufms.ui.home.BookCardAdapter adapter = new br.ufms.leiaufms.ui.home.BookCardAdapter(
                new br.ufms.leiaufms.ui.home.BookCardAdapter.Listener() {
                    @Override
                    public void onOpen(ListingRow row) {
                        Bundle args = new Bundle();
                        args.putLong(ListingDetailFragment.ARG_ID, row.anuncioId);
                        NavHostFragment.findNavController(ExploreFragment.this).navigate(R.id.listingDetailFragment, args);
                    }

                    @Override
                    public void onToggleWish(ListingRow row) {
                        br.ufms.leiaufms.core.util.AppExecutors.io().execute(() ->
                                new br.ufms.leiaufms.data.repository.SocialRepository(app).toggleWish(me, row.obraId));
                    }
                }).grid();
        binding.list.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2));
        binding.list.setAdapter(adapter);
        br.ufms.leiaufms.data.local.AppDatabase.get(requireContext()).socialDao().wishedObras(me)
                .observe(getViewLifecycleOwner(), ids -> adapter.setWished(ids == null ? new java.util.HashSet<>()
                        : new java.util.HashSet<>(ids)));
        String initialGenre = getArguments() == null ? null : getArguments().getString(ARG_GENRE);
        com.google.android.material.chip.Chip all = new com.google.android.material.chip.Chip(requireContext());
        all.setText(R.string.all_genres);
        all.setCheckable(true);
        all.setId(View.generateViewId());
        binding.genres.addView(all);
        int checkId = all.getId();
        for (String g : br.ufms.leiaufms.data.local.Generos.TODOS) {
            com.google.android.material.chip.Chip c = new com.google.android.material.chip.Chip(requireContext());
            c.setText(g);
            c.setCheckable(true);
            c.setId(View.generateViewId());
            c.setTag(g);
            binding.genres.addView(c);
            if (g.equals(initialGenre)) checkId = c.getId();
        }
        binding.genres.check(checkId);
        binding.genres.setOnCheckedStateChangeListener((g, ids) -> applyFilter());
        applyFilter();
        // Anúncio novo entra no topo: rola até ele em vez de ficar escondido acima da tela
        adapter.registerAdapterDataObserver(new androidx.recyclerview.widget.RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                if (positionStart == 0 && binding != null) binding.list.scrollToPosition(0);
            }
        });
        binding.empty.emptyIcon.setImageResource(R.drawable.ic_book);
        viewModel.listings().observe(getViewLifecycleOwner(), list -> render(adapter, list));

        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyFilter();
            }
        });
        binding.chipsFilter.setOnCheckedStateChangeListener((group, ids) -> applyFilter());
        binding.chipNear.setChecked(viewModel.radius() != null);
        binding.chipsRadius.setVisibility(viewModel.radius() != null ? View.VISIBLE : View.GONE);
        binding.chipNear.setOnCheckedChangeListener((chip, checked) -> {
            if (checked) requestLocation();
            else {
                viewModel.setRadius(null);
                binding.chipsRadius.setVisibility(View.GONE);
            }
        });
        binding.chipsRadius.setOnCheckedStateChangeListener((group, ids) -> {
            if (binding.chipNear.isChecked()) viewModel.setRadius(selectedRadius());
        });
        binding.fabNew.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.newListingFragment));
        binding.list.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull androidx.recyclerview.widget.RecyclerView rv, int dx, int dy) {
                if (dy > 8) binding.fabNew.shrink();
                else if (dy < -8) binding.fabNew.extend();
            }
        });
    }

    private void applyFilter() {
        int id = binding.chipsFilter.getCheckedChipId();
        String modalidade = id == R.id.chip_trade ? Anuncio.MODALIDADE_TROCA
                : id == R.id.chip_donation ? Anuncio.MODALIDADE_DOACAO : "";
        String q = binding.inputSearch.getText() == null ? "" : binding.inputSearch.getText().toString();
        View checkedGenre = binding.genres.findViewById(binding.genres.getCheckedChipId());
        String genero = checkedGenre == null || checkedGenre.getTag() == null ? "" : (String) checkedGenre.getTag();
        viewModel.setQuery(q, modalidade, genero);
    }

    private int selectedRadius() {
        int id = binding.chipsRadius.getCheckedChipId();
        if (id == R.id.radius_500) return 500;
        if (id == R.id.radius_1000) return 1000;
        if (id == R.id.radius_5000) return 5000;
        return 2000;
    }

    /** RF-27: permissão → GPS ligado → leitura única. Cada falha tem uma mensagem própria (§7). */
    private void requestLocation() {
        permissions.request(new PermissionHelper.Callback() {
            @Override
            public void onGranted() {
                fetchLocation();
            }

            @Override
            public void onDenied(boolean permanently) {
                if (binding == null) return;
                binding.chipNear.setChecked(false);
                showNotice(getString(R.string.location_denied), permanently ? R.string.action_open_settings : 0,
                        v -> PermissionHelper.openAppSettings(requireContext()));
            }
        }, LocationProvider.PERMISSIONS);
    }

    private void fetchLocation() {
        showNotice(getString(R.string.location_loading), 0, null);
        locationProvider.current(new LocationProvider.Callback() {
            @Override
            public void onLocation(android.location.Location location) {
                if (binding == null) return;
                hideNotice();
                viewModel.setLocation(location);
                binding.chipsRadius.setVisibility(View.VISIBLE);
                viewModel.setRadius(selectedRadius());
            }

            @Override
            public void onFailure(LocationProvider.Failure failure) {
                if (binding == null) return;
                binding.chipNear.setChecked(false);
                if (failure == LocationProvider.Failure.DISABLED) {
                    showNotice(getString(R.string.location_disabled), R.string.action_open_settings,
                            v -> startActivity(new android.content.Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
                } else if (failure == LocationProvider.Failure.NO_PERMISSION) {
                    showNotice(getString(R.string.location_denied), 0, null);
                } else {
                    showNotice(getString(R.string.location_unavailable), R.string.action_try_again, v -> {
                        binding.chipNear.setChecked(true);
                    });
                }
            }
        });
    }

    private void showNotice(String text, int action, View.OnClickListener listener) {
        binding.notice.setVisibility(View.VISIBLE);
        binding.noticeText.setText(text);
        if (action != 0) {
            binding.noticeAction.setVisibility(View.VISIBLE);
            binding.noticeAction.setText(action);
            binding.noticeAction.setOnClickListener(listener);
        } else {
            binding.noticeAction.setVisibility(View.GONE);
        }
    }

    private void hideNotice() {
        binding.notice.setVisibility(View.GONE);
    }

    private void render(br.ufms.leiaufms.ui.home.BookCardAdapter adapter, List<ListingRow> list) {
        adapter.submitList(list);
        boolean empty = list == null || list.isEmpty();
        binding.empty.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.empty.emptyAction.setVisibility(View.GONE);
        if (!empty) return;
        String q = viewModel.query();
        Integer r = viewModel.radius();
        if (!q.isEmpty()) {
            binding.empty.emptyTitle.setText(R.string.empty_explore_title);
            binding.empty.emptyMessage.setText(getString(R.string.explore_empty_search, q));
        } else if (r != null) {
            binding.empty.emptyTitle.setText(R.string.empty_explore_title);
            com.google.android.material.chip.Chip chip =
                    binding.chipsRadius.findViewById(binding.chipsRadius.getCheckedChipId());
            binding.empty.emptyMessage.setText(getString(R.string.explore_empty_radius,
                    chip == null ? "" : chip.getText()));
        } else {
            binding.empty.emptyTitle.setText(R.string.empty_explore_title);
            binding.empty.emptyMessage.setText(R.string.empty_explore_message);
            binding.empty.emptyAction.setVisibility(View.VISIBLE);
            binding.empty.emptyAction.setText(R.string.action_new_listing);
            binding.empty.emptyAction.setOnClickListener(v ->
                    NavHostFragment.findNavController(this).navigate(R.id.newListingFragment));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        locationProvider.cancel();
        binding = null;
    }
}
