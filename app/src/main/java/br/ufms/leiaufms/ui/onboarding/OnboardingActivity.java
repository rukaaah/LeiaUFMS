package br.ufms.leiaufms.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;


import java.util.List;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.ItemEstante;
import br.ufms.leiaufms.data.local.entity.Obra;
import br.ufms.leiaufms.data.repository.SocialRepository;
import br.ufms.leiaufms.databinding.ActivityOnboardingBinding;
import br.ufms.leiaufms.databinding.PageOnboardingBinding;
import br.ufms.leiaufms.ui.common.FieldErrors;
import br.ufms.leiaufms.ui.common.InsetsHelper;
import br.ufms.leiaufms.ui.entry.EntryActivity;
import br.ufms.leiaufms.ui.main.MainActivity;

/**
 * Onboarding (jornada §3, docs/08): o que é o app, créditos, encontro em ponto seguro,
 * localização só nos 10 minutos antes do encontro (RF-41) e preferências opcionais
 * (o que está lendo e um desejo), que alimentam o "Pra você agora" do Início.
 */
public class OnboardingActivity extends AppCompatActivity {

    private static final int[][] PAGES = {
            {R.drawable.ic_book, R.string.onb_1_title, R.string.onb_1_text, R.drawable.bg_circle_primary},
            {R.drawable.ic_donate, R.string.onb_2_title, R.string.onb_2_text, R.drawable.bg_circle_tertiary},
            {R.drawable.ic_place, R.string.onb_3_title, R.string.onb_3_text, R.drawable.bg_circle_success},
            {R.drawable.ic_my_location, R.string.onb_4_title, R.string.onb_4_text, R.drawable.bg_circle_primary},
            {R.drawable.ic_library, R.string.onb_5_title, R.string.onb_5_text, R.drawable.bg_circle_tertiary},
    };

    private ActivityOnboardingBinding binding;
    private PageOnboardingBinding setup;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        if (!SessionManager.get(this).hasValidatedSession()) {
            startActivity(new Intent(this, EntryActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }
        userId = SessionManager.get(this).currentUserId();
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsHelper.applySystemBars(binding.root);
        InsetsHelper.styleBars(this);

        binding.pager.setAdapter(new PageAdapter());
        buildDots();
        binding.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                boolean last = position == PAGES.length - 1;
                renderDots(position);
                binding.buttonNext.setText(last ? R.string.action_start : R.string.action_continue);
                binding.buttonSkip.setVisibility(last ? View.INVISIBLE : View.VISIBLE);
            }
        });
        binding.buttonNext.setOnClickListener(v -> {
            int p = binding.pager.getCurrentItem();
            if (p < PAGES.length - 1) binding.pager.setCurrentItem(p + 1);
            else finishOnboarding(true);
        });
        binding.buttonSkip.setOnClickListener(v -> binding.pager.setCurrentItem(PAGES.length - 1));
    }

    /** Pontos fixos (8 dp); a página atual vira uma pílula de 24 dp, sem esticar os outros. */
    private void buildDots() {
        int size = getResources().getDimensionPixelSize(R.dimen.space_xs);
        int gap = getResources().getDimensionPixelSize(R.dimen.space_xxs);
        for (int i = 0; i < PAGES.length; i++) {
            View dot = new View(this);
            android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(size, size);
            lp.setMarginStart(gap);
            lp.setMarginEnd(gap);
            dot.setLayoutParams(lp);
            dot.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            binding.dots.addView(dot);
        }
        renderDots(0);
    }

    private void renderDots(int current) {
        int size = getResources().getDimensionPixelSize(R.dimen.space_xs);
        for (int i = 0; i < binding.dots.getChildCount(); i++) {
            View dot = binding.dots.getChildAt(i);
            boolean on = i == current;
            dot.setBackgroundResource(on ? R.drawable.bg_page_dot_active : R.drawable.bg_page_dot);
            dot.getLayoutParams().width = on ? size * 3 : size;
            dot.requestLayout();
        }
        binding.dots.setContentDescription(getString(R.string.onb_page, current + 1, PAGES.length));
    }

    private void finishOnboarding(boolean savePrefs) {
        OnboardingState.markDone(this, userId);
        if (savePrefs && setup != null) {
            String rt = FieldErrors.text(setup.inputReadingTitle).trim();
            String ra = FieldErrors.text(setup.inputReadingAuthor).trim();
            String wt = FieldErrors.text(setup.inputWishTitle).trim();
            String wa = FieldErrors.text(setup.inputWishAuthor).trim();
            long uid = userId;
            android.content.Context app = getApplicationContext();
            AppExecutors.io().execute(() -> {
                SocialRepository repo = new SocialRepository(app);
                if (!rt.isEmpty()) repo.addToShelf(uid, rt, ra.isEmpty() ? "—" : ra, ItemEstante.STATUS_LENDO);
                if (!wt.isEmpty()) repo.addWish(uid, wt, wa.isEmpty() ? "—" : wa);
            });
        }
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void bindSuggestions(PageOnboardingBinding b) {
        br.ufms.leiaufms.ui.common.BookSuggestions.attach(b.inputReadingTitle, b.inputReadingAuthor);
        br.ufms.leiaufms.ui.common.BookSuggestions.attach(b.inputWishTitle, b.inputWishAuthor);
    }

    private static void fillAuthor(List<Obra> obras, String titulo, android.widget.EditText author) {
        for (Obra o : obras) if (o.titulo.equals(titulo)) author.setText(o.autor);
    }

    private class PageAdapter extends RecyclerView.Adapter<PageAdapter.Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(PageOnboardingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            int[] page = PAGES[position];
            h.b.icon.setImageResource(page[0]);
            h.b.icon.setColorFilter(getColor(position == 1 || position == 4 ? R.color.leia_tertiary
                    : position == 2 ? R.color.leia_success : R.color.leia_primary));
            h.b.art.setBackgroundResource(page[3]);
            h.b.title.setText(page[1]);
            h.b.text.setText(page[2]);
            boolean last = position == PAGES.length - 1;
            h.b.setup.setVisibility(last ? View.VISIBLE : View.GONE);
            if (last && setup != h.b) {
                setup = h.b;
                bindSuggestions(h.b);
            }
        }

        @Override
        public int getItemCount() {
            return PAGES.length;
        }

        class Holder extends RecyclerView.ViewHolder {
            final PageOnboardingBinding b;

            Holder(PageOnboardingBinding b) {
                super(b.getRoot());
                this.b = b;
            }
        }
    }
}
