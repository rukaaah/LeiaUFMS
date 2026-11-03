package br.ufms.leiaufms.ui.home;

import android.content.Context;
import android.os.Bundle;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.data.repository.SocialRepository;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.location.LocationProvider;
import br.ufms.leiaufms.core.permission.PermissionHelper;
import br.ufms.leiaufms.data.local.model.ListingRow;
import br.ufms.leiaufms.data.local.model.PedidoRow;
import br.ufms.leiaufms.data.local.model.ShelfRow;
import br.ufms.leiaufms.data.pedido.PedidoEstado;
import br.ufms.leiaufms.databinding.FragmentHomeBinding;
import br.ufms.leiaufms.databinding.ItemActionCardBinding;
import br.ufms.leiaufms.ui.common.Labels;
import br.ufms.leiaufms.ui.common.TimeText;
import br.ufms.leiaufms.ui.listing.ListingDetailFragment;
import br.ufms.leiaufms.ui.trade.TradeDetailFragment;

/**
 * Início (jornada §3, momentos 2, 3 e 9): responde "o que tem pra mim agora?".
 * Diferencial: sempre mostra o próximo livro e o próximo passo.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private PermissionHelper permissions;
    private LocationProvider locationProvider;
    private List<PedidoRow> received = new ArrayList<>();
    private List<PedidoRow> sent = new ArrayList<>();
    private List<ShelfRow> wishes = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissions = new PermissionHelper(this);
        locationProvider = new LocationProvider(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        BookCardAdapter.Listener listener = new BookCardAdapter.Listener() {
            @Override
            public void onOpen(ListingRow row) {
                openListing(row.anuncioId);
            }

            @Override
            public void onToggleWish(ListingRow row) {
                viewModel.toggleWish(row.obraId);
            }
        };
        BookCardAdapter nearAdapter = new BookCardAdapter(listener);
        BookCardAdapter courseAdapter = new BookCardAdapter(listener);
        BookCardAdapter freshAdapter = new BookCardAdapter(listener);
        binding.nearList.setAdapter(nearAdapter);
        binding.courseList.setAdapter(courseAdapter);
        binding.freshList.setAdapter(freshAdapter);
        BookCardAdapter becauseAdapter = new BookCardAdapter(listener);
        BookCardAdapter genreAdapter = new BookCardAdapter(listener);
        binding.becauseList.setAdapter(becauseAdapter);
        binding.genreList.setAdapter(genreAdapter);
        // "Porque você leu X": mesmo autor ou gênero do último livro da estante
        viewModel.lastRead().observe(getViewLifecycleOwner(), last -> {
            if (last == null) {
                showSection(binding.becauseTitle, binding.becauseList, false);
                return;
            }
            binding.becauseTitle.setText(getString(R.string.home_because, last.titulo));
            viewModel.because(last.obraId).observe(getViewLifecycleOwner(), list -> {
                becauseAdapter.submitList(list);
                showSection(binding.becauseTitle, binding.becauseList, list != null && !list.isEmpty());
            });
        });
        // Gênero que a pessoa mais lê, deseja ou troca
        viewModel.topGenre().observe(getViewLifecycleOwner(), genero -> {
            if (genero == null) {
                showSection(binding.genreTitle, binding.genreList, false);
                return;
            }
            binding.genreTitle.setText(getString(R.string.home_more_genre, genero));
            viewModel.byGenre(genero).observe(getViewLifecycleOwner(), list -> {
                genreAdapter.submitList(list);
                showSection(binding.genreTitle, binding.genreList, list != null && !list.isEmpty());
            });
        });
        for (String g : br.ufms.leiaufms.data.local.Generos.TODOS) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(requireContext());
            chip.setText(g);
            chip.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString(br.ufms.leiaufms.ui.explore.ExploreFragment.ARG_GENRE, g);
                NavHostFragment.findNavController(this).navigate(R.id.exploreFragment, args);
            });
            binding.genres.addView(chip);
        }
        viewModel.wished().observe(getViewLifecycleOwner(), ids -> {
            HashSet<Long> set = ids == null ? new HashSet<>() : new HashSet<>(ids);
            becauseAdapter.setWished(set);
            genreAdapter.setWished(set);
        });

        viewModel.user().observe(getViewLifecycleOwner(), u -> {
            if (u == null) return;
            String first = u.nome.trim().split("\\s+")[0];
            binding.hello.setText(getString(R.string.home_hello, first));
            binding.courseSub.setText(getString(R.string.home_course_sub, u.curso));
        });
        viewModel.credits().observe(getViewLifecycleOwner(), c -> {
            int n = c == null ? 0 : c;
            binding.credits.setText(getResources().getQuantityString(R.plurals.credits_available, n, n));
            binding.credits.setContentDescription(getString(R.string.credits_balance_description) + ": "
                    + getResources().getQuantityString(R.plurals.credits_available, n, n));
        });
        viewModel.wished().observe(getViewLifecycleOwner(), ids -> {
            HashSet<Long> set = ids == null ? new HashSet<>() : new HashSet<>(ids);
            nearAdapter.setWished(set);
            courseAdapter.setWished(set);
            freshAdapter.setWished(set);
        });
        viewModel.near().observe(getViewLifecycleOwner(), list -> {
            nearAdapter.submitList(list);
            renderNear(list);
        });
        // RF-45: livros das disciplinas do curso (catálogo); coração deseja, toque abre a página do livro
        android.content.Context appCtx = requireContext().getApplicationContext();
        long meId = br.ufms.leiaufms.core.security.SessionManager.get(appCtx).currentUserId();
        CatalogCardAdapter gradeAdapter = new CatalogCardAdapter(new CatalogCardAdapter.Listener() {
            @Override
            public void onOpen(br.ufms.leiaufms.data.local.entity.LivroCatalogo l) {
                AppExecutors.io().execute(() -> {
                    long obraId = new SocialRepository(appCtx).obra(l.titulo, l.autor);
                    AppExecutors.main(() -> {
                        if (binding == null) return;
                        Bundle args = new Bundle();
                        args.putLong(br.ufms.leiaufms.ui.book.ObraFragment.ARG_ID, obraId);
                        NavHostFragment.findNavController(HomeFragment.this).navigate(R.id.obraFragment, args);
                    });
                });
            }

            @Override
            public void onWish(br.ufms.leiaufms.data.local.entity.LivroCatalogo l) {
                AppExecutors.io().execute(() -> {
                    new SocialRepository(appCtx).addWish(meId, l.titulo, l.autor);
                    AppExecutors.main(() -> {
                        if (binding != null) com.google.android.material.snackbar.Snackbar.make(binding.getRoot(),
                                R.string.wish_saved, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                    });
                });
            }
        });
        binding.gradeList.setAdapter(gradeAdapter);
        viewModel.grade().observe(getViewLifecycleOwner(), list -> {
            gradeAdapter.submit(list);
            boolean show = list != null && !list.isEmpty();
            binding.gradeTitle.setVisibility(show ? View.VISIBLE : View.GONE);
            binding.gradeList.setVisibility(show ? View.VISIBLE : View.GONE);
        });
        viewModel.course().observe(getViewLifecycleOwner(), list -> {
            courseAdapter.submitList(list);
            boolean show = list != null && !list.isEmpty();
            binding.courseTitle.setVisibility(show ? View.VISIBLE : View.GONE);
            binding.courseSub.setVisibility(show ? View.VISIBLE : View.GONE);
            binding.courseList.setVisibility(show ? View.VISIBLE : View.GONE);
        });
        viewModel.fresh().observe(getViewLifecycleOwner(), freshAdapter::submitList);
        viewModel.received().observe(getViewLifecycleOwner(), l -> {
            received = l == null ? new ArrayList<>() : l;
            renderForYou();
        });
        viewModel.sent().observe(getViewLifecycleOwner(), l -> {
            sent = l == null ? new ArrayList<>() : l;
            renderForYou();
        });
        viewModel.wishes().observe(getViewLifecycleOwner(), l -> {
            wishes = l == null ? new ArrayList<>() : l;
            renderForYou();
        });

        binding.search.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.exploreFragment));
        binding.buttonAll.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.exploreFragment));
        binding.fabNew.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.newListingFragment));
        binding.scroll.setOnScrollChangeListener((androidx.core.widget.NestedScrollView.OnScrollChangeListener)
                (sv, x, y, ox, oy) -> {
                    if (y > oy + 8) binding.fabNew.shrink();
                    else if (y < oy - 8) binding.fabNew.extend();
                });
        binding.nearInviteAction.setOnClickListener(v -> askLocation());

        // RNF-07: só lê a posição se a permissão já existe; senão, convida (não pede ao abrir)
        if (PermissionHelper.anyGranted(requireContext(), LocationProvider.PERMISSIONS)) fetchLocation();
    }

    private static void showSection(View title, View list, boolean show) {
        title.setVisibility(show ? View.VISIBLE : View.GONE);
        list.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void renderNear(List<ListingRow> list) {
        boolean hasLocation = viewModel.hasLocation();
        boolean any = list != null && !list.isEmpty();
        binding.nearSub.setText(R.string.home_near_sub);
        binding.nearList.setVisibility(any ? View.VISIBLE : View.GONE);
        binding.nearInvite.setVisibility(any ? View.GONE : View.VISIBLE);
        if (!hasLocation) {
            binding.nearInviteText.setText(R.string.home_near_invite);
            binding.nearInviteAction.setVisibility(View.VISIBLE);
        } else if (!any) {
            binding.nearInviteText.setText(R.string.home_near_none);
            binding.nearInviteAction.setVisibility(View.GONE);
        }
    }

    /** "Pra você agora": só ações reais; o bloco some quando não há nada (sem caixa vazia). */
    private void renderForYou() {
        if (binding == null) return;
        binding.forYouList.removeAllViews();
        long me = viewModel.userId;
        int count = 0;
        for (PedidoRow r : received) {
            if (PedidoEstado.PENDENTE.equals(r.pedido.estado)) {
                addAction(R.drawable.ic_swap, R.drawable.bg_circle_primary, R.color.leia_primary,
                        getString(R.string.act_answer, r.solicitanteNome, r.titulo), getString(R.string.act_answer_sub),
                        () -> openTrade(r.pedido.id));
                count++;
            }
        }
        List<PedidoRow> mine = new ArrayList<>(received);
        mine.addAll(sent);
        for (PedidoRow r : mine) {
            if (!PedidoEstado.ACEITO.equals(r.pedido.estado)) continue;
            String other = r.pedido.donoId == me ? r.solicitanteNome : r.donoNome;
            if (r.pedido.encontroEm == null) {
                addAction(R.drawable.ic_place, R.drawable.bg_circle_success, R.color.leia_success,
                        getString(R.string.act_schedule, r.titulo), getString(R.string.act_with, other),
                        () -> openTrade(r.pedido.id));
            } else if (!r.pedido.encontroAceito) {
                // RF-44: proposta de horário esperando a pessoa
                if (Labels.proposedBy(r.pedido, me)) continue;
                addAction(R.drawable.ic_schedule, R.drawable.bg_circle_success, R.color.leia_success,
                        getString(R.string.act_meeting_proposal, other, r.titulo),
                        (r.pontoNome == null ? "" : r.pontoNome + " · ") + TimeText.meeting(requireContext(), r.pedido.encontroEm),
                        () -> openTrade(r.pedido.id));
            } else if (inMeetingBar(r.pedido.encontroEm)) {
                // O encontro já está na faixa fixa acima das abas: não repetir aqui
                continue;
            } else if (android.text.format.DateUtils.isToday(r.pedido.encontroEm)
                    && r.pedido.encontroEm > System.currentTimeMillis()) {
                addAction(R.drawable.ic_event, R.drawable.bg_circle_success, R.color.leia_success,
                        getString(R.string.act_meeting_today, r.titulo),
                        (r.pontoNome == null ? "" : r.pontoNome + " · ") + TimeText.hour(r.pedido.encontroEm),
                        () -> openTrade(r.pedido.id));
            } else if (Labels.needsAction(r.pedido, me)) {
                addAction(R.drawable.ic_check_circle, R.drawable.bg_circle_success, R.color.leia_success,
                        getString(R.string.act_confirm, r.titulo), getString(R.string.act_with, other),
                        () -> openTrade(r.pedido.id));
            } else {
                continue;
            }
            count++;
        }
        for (ShelfRow w : wishes) {
            if (w.disponiveis > 0 && w.anuncioId != null) {
                addAction(R.drawable.ic_favorite, R.drawable.bg_circle_tertiary, R.color.leia_tertiary,
                        getString(R.string.act_wish, w.titulo), getResources().getQuantityString(R.plurals.wish_copies, w.disponiveis, w.disponiveis),
                        () -> openListing(w.anuncioId));
                count++;
            }
        }
        binding.forYou.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    /** Mesma janela da faixa do encontro (MainActivity): de 60 min antes a 30 min depois. */
    private static boolean inMeetingBar(long encontroEm) {
        long diff = encontroEm - System.currentTimeMillis();
        return diff <= 60 * 60_000L && diff >= -30 * 60_000L;
    }

    private void addAction(int icon, int bg, int tint, String title, String subtitle, Runnable onClick) {
        Context c = requireContext();
        ItemActionCardBinding b = ItemActionCardBinding.inflate(getLayoutInflater(), binding.forYouList, false);
        b.icon.setImageResource(icon);
        b.icon.setColorFilter(c.getColor(tint));
        b.iconBox.setBackgroundResource(bg);
        b.title.setText(title);
        b.subtitle.setText(subtitle);
        b.getRoot().setContentDescription(title + ". " + subtitle);
        b.getRoot().setOnClickListener(v -> onClick.run());
        binding.forYouList.addView(b.getRoot());
    }

    private void askLocation() {
        permissions.request(new PermissionHelper.Callback() {
            @Override
            public void onGranted() {
                fetchLocation();
            }

            @Override
            public void onDenied(boolean permanently) {
                if (binding == null) return;
                binding.nearInviteText.setText(R.string.location_denied);
                if (permanently) {
                    binding.nearInviteAction.setText(R.string.action_open_settings);
                    binding.nearInviteAction.setOnClickListener(v -> PermissionHelper.openAppSettings(requireContext()));
                }
            }
        }, LocationProvider.PERMISSIONS);
    }

    private void fetchLocation() {
        locationProvider.current(new LocationProvider.Callback() {
            @Override
            public void onLocation(android.location.Location l) {
                if (binding != null) viewModel.setLocation(l);
            }

            @Override
            public void onFailure(LocationProvider.Failure f) {
                if (binding == null) return;
                binding.nearInviteText.setText(f == LocationProvider.Failure.DISABLED
                        ? R.string.location_disabled : R.string.location_unavailable);
            }
        });
    }

    private void openListing(long anuncioId) {
        Bundle args = new Bundle();
        args.putLong(ListingDetailFragment.ARG_ID, anuncioId);
        NavHostFragment.findNavController(this).navigate(R.id.listingDetailFragment, args);
    }

    private void openTrade(long pedidoId) {
        Bundle args = new Bundle();
        args.putLong(TradeDetailFragment.ARG_ID, pedidoId);
        NavHostFragment.findNavController(this).navigate(R.id.tradeDetailFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        locationProvider.cancel();
        binding = null;
    }
}
