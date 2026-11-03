package br.ufms.leiaufms.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.data.local.entity.LivroCatalogo;
import br.ufms.leiaufms.data.repository.CoverRepository;
import br.ufms.leiaufms.databinding.ItemBookCardBinding;
import br.ufms.leiaufms.ui.common.Covers;

/** "Da sua grade" (RF-45): livros do catálogo ligados às disciplinas do curso; coração = desejar. */
public class CatalogCardAdapter extends RecyclerView.Adapter<CatalogCardAdapter.Holder> {

    public interface Listener {
        void onOpen(LivroCatalogo l);

        void onWish(LivroCatalogo l);
    }

    private final List<LivroCatalogo> items = new ArrayList<>();
    private final Listener listener;

    public CatalogCardAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<LivroCatalogo> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemBookCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        LivroCatalogo l = items.get(position);
        Context c = h.itemView.getContext();
        h.b.title.setText(l.titulo);
        h.b.meta.setText(l.autor);
        h.b.badge.setVisibility(View.GONE);
        File cached = new File(new File(c.getFilesDir(), "capas"), "cat_" + l.id + ".jpg");
        Covers.bind(h.b.cover.coverImage, h.b.cover.coverText, cached.exists() ? cached.getAbsolutePath() : null, l.titulo);
        if (!cached.exists() && l.capaOl != null) {
            h.b.cover.coverImage.setTag(R.id.cover, l.id);
            Context app = c.getApplicationContext();
            AppExecutors.io().execute(() -> {
                String path = new CoverRepository(app).catalogCover(l.id, l.capaOl);
                AppExecutors.main(() -> {
                    Object tag = h.b.cover.coverImage.getTag(R.id.cover);
                    if (path != null && tag != null && tag.equals(l.id)) {
                        Covers.bind(h.b.cover.coverImage, h.b.cover.coverText, path, l.titulo);
                    }
                });
            });
        }
        h.b.heart.setImageResource(R.drawable.ic_favorite_border);
        h.b.heart.setContentDescription(c.getString(R.string.wish_add) + ": " + l.titulo);
        h.b.heart.setOnClickListener(v -> {
            v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(120)
                    .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()).start();
            listener.onWish(l);
        });
        h.b.card.setContentDescription(c.getString(R.string.wish_card_description, l.titulo, l.autor, ""));
        h.b.card.setOnClickListener(v -> listener.onOpen(l));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemBookCardBinding b;

        Holder(ItemBookCardBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
