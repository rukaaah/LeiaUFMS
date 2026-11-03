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
import br.ufms.leiaufms.databinding.ItemBookCardBinding;
import br.ufms.leiaufms.ui.common.Covers;

/** Grade de desejos: capa grande, selo verde "Disponível" ou neutro "Aguardando", coração cheio para tirar. */
public class WishCardAdapter extends RecyclerView.Adapter<WishCardAdapter.Holder> {

    public interface Listener {
        void onOpen(ShelfRow row);

        void onRemove(ShelfRow row);
    }

    private final List<ShelfRow> items = new ArrayList<>();
    private final Listener listener;

    public WishCardAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<ShelfRow> rows) {
        items.clear();
        if (rows != null) {
            // Disponíveis primeiro: o olho vai direto ao que dá para pedir agora
            for (ShelfRow r : rows) if (r.disponiveis > 0) items.add(r);
            for (ShelfRow r : rows) if (r.disponiveis == 0) items.add(r);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookCardBinding b = ItemBookCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        ViewGroup.LayoutParams lp = b.getRoot().getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        b.getRoot().setLayoutParams(lp);
        return new Holder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        ShelfRow r = items.get(position);
        Context c = h.itemView.getContext();
        Covers.bind(h.b.cover.coverImage, h.b.cover.coverText, r.capaPath, r.titulo);
        h.b.title.setText(r.titulo);
        h.b.meta.setText(r.autor);
        boolean available = r.disponiveis > 0;
        h.b.badge.setVisibility(View.VISIBLE);
        h.b.badge.setBackgroundResource(available ? R.drawable.bg_badge_walk : R.drawable.bg_badge_neutral);
        h.b.badge.setTextColor(c.getColor(available ? R.color.leia_on_success_container : R.color.leia_on_surface_variant));
        h.b.badge.setText(available ? R.string.wish_badge_available : R.string.wish_badge_waiting);
        h.b.cover.getRoot().setAlpha(available ? 1f : 0.75f);
        h.b.heart.setImageResource(R.drawable.ic_favorite);
        h.b.heart.setColorFilter(c.getColor(R.color.leia_primary));
        h.b.heart.setContentDescription(c.getString(R.string.wish_remove) + ": " + r.titulo);
        h.b.heart.setOnClickListener(v -> listener.onRemove(r));
        h.b.card.setContentDescription(c.getString(R.string.wish_card_description, r.titulo, r.autor,
                c.getString(available ? R.string.wish_badge_available : R.string.wish_badge_waiting)));
        h.b.card.setOnClickListener(v -> listener.onOpen(r));
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
