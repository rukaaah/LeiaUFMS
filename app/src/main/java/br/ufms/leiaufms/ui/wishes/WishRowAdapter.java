package br.ufms.leiaufms.ui.wishes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.data.local.model.ShelfRow;
import br.ufms.leiaufms.databinding.ItemWishRowBinding;
import br.ufms.leiaufms.ui.common.Covers;

/** Desejos que ainda não foram anunciados: lista compacta com prova social leve ("mais 3 querem"). */
public class WishRowAdapter extends RecyclerView.Adapter<WishRowAdapter.Holder> {

    private final List<ShelfRow> items = new ArrayList<>();
    private final WishCardAdapter.Listener listener;

    public WishRowAdapter(WishCardAdapter.Listener listener) {
        this.listener = listener;
    }

    public void submit(List<ShelfRow> rows) {
        items.clear();
        if (rows != null) items.addAll(rows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemWishRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        ShelfRow r = items.get(position);
        Context c = h.itemView.getContext();
        Covers.bind(h.b.cover.coverImage, h.b.cover.coverText, r.capaPath, r.titulo);
        h.b.title.setText(r.titulo);
        h.b.author.setText(r.autor);
        String others = r.outrosQuerem > 0
                ? c.getResources().getQuantityString(R.plurals.wish_others, r.outrosQuerem, r.outrosQuerem) : null;
        h.b.others.setVisibility(others == null ? View.GONE : View.VISIBLE);
        h.b.others.setText(others);
        h.b.heart.setContentDescription(c.getString(R.string.wish_remove) + ": " + r.titulo);
        h.b.heart.setOnClickListener(v -> listener.onRemove(r));
        h.b.row.setContentDescription(c.getString(R.string.wish_card_description, r.titulo, r.autor,
                c.getString(R.string.wish_badge_waiting) + (others == null ? "" : ". " + others)));
        h.b.row.setOnClickListener(v -> listener.onOpen(r));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemWishRowBinding b;

        Holder(ItemWishRowBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
