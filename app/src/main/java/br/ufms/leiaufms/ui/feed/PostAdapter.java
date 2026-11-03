package br.ufms.leiaufms.ui.feed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.data.local.entity.PostMural;
import br.ufms.leiaufms.data.local.model.PostRow;
import br.ufms.leiaufms.databinding.ItemFeedEndBinding;
import br.ufms.leiaufms.databinding.ItemPostBinding;
import br.ufms.leiaufms.ui.common.Covers;
import br.ufms.leiaufms.ui.common.TimeText;

/** Mural em grade de 2 colunas (Pinterest) que termina em "Você está em dia" (PC-11). */
public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Listener {
        void onReact(PostRow p, String tipo);

        void onOpen(PostRow p);

        void onLongPress(PostRow p);
    }

    private static final int POST = 0;
    private static final int END = 1;

    private final List<PostRow> items = new ArrayList<>();
    private final Listener listener;

    public PostAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<PostRow> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position == items.size() ? END : POST;
    }

    @Override
    public int getItemCount() {
        return items.isEmpty() ? 0 : items.size() + 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == END) {
            View v = ItemFeedEndBinding.inflate(inf, parent, false).getRoot();
            StaggeredGridLayoutManager.LayoutParams lp = new StaggeredGridLayoutManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setFullSpan(true);
            v.setLayoutParams(lp);
            return new RecyclerView.ViewHolder(v) {
            };
        }
        return new Holder(ItemPostBinding.inflate(inf, parent, false));
    }

    static int[] colors(String tipo) {
        switch (tipo) {
            case PostMural.TIPO_PROCURO:
                return new int[]{R.color.post_procuro_container, R.color.post_procuro_on, R.string.post_type_procuro};
            case PostMural.TIPO_DOACAO:
                return new int[]{R.color.post_doacao_container, R.color.post_doacao_on, R.string.post_type_doacao};
            case PostMural.TIPO_TROCA_CONCLUIDA:
                return new int[]{R.color.post_troca_container, R.color.post_troca_on, R.string.post_type_troca};
            default:
                return new int[]{R.color.post_dica_container, R.color.post_dica_on, R.string.post_type_dica};
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (!(holder instanceof Holder)) return;
        Holder h = (Holder) holder;
        PostRow p = items.get(position);
        Context c = h.itemView.getContext();
        int[] col = colors(p.tipo);
        android.graphics.drawable.GradientDrawable tag = new android.graphics.drawable.GradientDrawable();
        tag.setColor(c.getColor(col[0]));
        tag.setCornerRadius(12 * c.getResources().getDisplayMetrics().density);
        h.b.tag.setBackground(tag);
        h.b.tag.setTextColor(c.getColor(col[1]));
        h.b.tag.setText(col[2]);
        Covers.bind(h.b.cover.coverImage, h.b.cover.coverText, p.capaPath, p.titulo);
        h.b.book.setText(p.titulo);
        h.b.text.setText(p.texto);
        String when = TimeText.ago(c, p.criadoEm);
        h.b.meta.setText(p.autorNome + " · " + when);
        bindReaction(h.b.reactUseful, p.meuUtil, p.uteis, R.string.react_useful, R.plurals.react_useful_count,
                R.drawable.ic_thumb_up, R.drawable.ic_thumb_up, c);
        bindReaction(h.b.reactWant, p.meuQuero, p.queros, R.string.react_want, R.plurals.react_want_count,
                R.drawable.ic_favorite, R.drawable.ic_favorite_border, c);
        h.b.reactUseful.setOnClickListener(v -> {
            bounce(v);
            listener.onReact(p, br.ufms.leiaufms.data.local.entity.Reacao.TIPO_UTIL);
        });
        h.b.reactWant.setOnClickListener(v -> {
            bounce(v);
            listener.onReact(p, br.ufms.leiaufms.data.local.entity.Reacao.TIPO_QUERO);
        });
        h.b.card.setOnClickListener(v -> listener.onOpen(p));
        h.b.card.setOnLongClickListener(v -> {
            listener.onLongPress(p);
            return true;
        });
        h.b.card.setContentDescription(c.getString(R.string.post_description, c.getString(col[2]), p.autorNome,
                p.titulo, p.texto, when, h.b.reactUseful.getText() + ", " + h.b.reactWant.getText()));
    }

    /** Reação ativa: cor de marca e ícone cheio; contagem em texto discreto, nunca número grande. */
    private static void bindReaction(com.google.android.material.button.MaterialButton b, boolean mine, int count,
                                     int label, int countLabel, int iconOn, int iconOff, Context c) {
        b.setText(count > 0 ? c.getResources().getQuantityString(countLabel, count, count) : c.getString(label));
        b.setIconResource(mine ? iconOn : iconOff);
        int color = c.getColor(mine ? R.color.leia_primary : R.color.leia_on_surface_variant);
        b.setTextColor(color);
        b.setIconTint(android.content.res.ColorStateList.valueOf(color));
        androidx.core.view.ViewCompat.setStateDescription(b, mine ? c.getString(R.string.state_selected) : null);
    }

    private static void bounce(View v) {
        v.animate().scaleX(0.94f).scaleY(0.94f).setDuration(90)
                .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(140).start()).start();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemPostBinding b;

        Holder(ItemPostBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
