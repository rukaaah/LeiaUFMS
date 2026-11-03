package br.ufms.leiaufms.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Set;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.location.WalkEstimate;
import br.ufms.leiaufms.data.local.model.ListingRow;
import br.ufms.leiaufms.databinding.ItemBookCardBinding;
import br.ufms.leiaufms.ui.common.Covers;
import br.ufms.leiaufms.ui.common.Labels;

/**
 * Card de carrossel (DESIGN.md §6, referência Airbnb): capa grande, selo "A pé · N min" sobre a capa
 * e coração de desejo no canto. É o elemento próprio do LeiaUFMS.
 */
public class BookCardAdapter extends ListAdapter<ListingRow, BookCardAdapter.Holder> {

    public interface Listener {
        void onOpen(ListingRow row);

        void onToggleWish(ListingRow row);
    }

    private final Listener listener;
    private Set<Long> wished = Collections.emptySet();

    public BookCardAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<ListingRow>() {
            @Override
            public boolean areItemsTheSame(@NonNull ListingRow a, @NonNull ListingRow b) {
                return a.anuncioId == b.anuncioId;
            }

            @Override
            public boolean areContentsTheSame(@NonNull ListingRow a, @NonNull ListingRow b) {
                return java.util.Objects.equals(a.distancia, b.distancia)
                        && java.util.Objects.equals(a.imagem(), b.imagem());
            }
        });
        this.listener = listener;
    }

    public void setWished(Set<Long> obras) {
        wished = obras == null ? Collections.emptySet() : obras;
        notifyItemRangeChanged(0, getItemCount());
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemBookCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        ListingRow r = getItem(position);
        Context c = h.itemView.getContext();
        Covers.bind(h.b.cover.coverImage, h.b.cover.coverText, r.imagem(), r.titulo);
        h.b.title.setText(r.titulo);
        String distance = null;
        if (r.distancia != null && WalkEstimate.isWalkable(r.distancia)) {
            h.b.badge.setVisibility(View.VISIBLE);
            h.b.badge.setBackgroundResource(R.drawable.bg_badge_walk);
            h.b.badge.setTextColor(c.getColor(R.color.leia_on_success_container));
            h.b.badge.setText(c.getString(R.string.badge_walk, WalkEstimate.minutes(r.distancia)));
            distance = Labels.distance(c, r.distancia);
        } else if (r.distancia != null) {
            h.b.badge.setVisibility(View.VISIBLE);
            h.b.badge.setBackgroundResource(R.drawable.bg_badge_neutral);
            h.b.badge.setTextColor(c.getColor(R.color.leia_on_background));
            distance = Labels.distance(c, r.distancia);
            h.b.badge.setText(distance);
        } else {
            h.b.badge.setVisibility(View.GONE);
        }
        String meta = Labels.modality(c, r.modalidade) + " · " + r.donoNome;
        h.b.meta.setText(meta);
        boolean isWished = wished.contains(r.obraId);
        h.b.heart.setImageResource(isWished ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        h.b.heart.setColorFilter(c.getColor(isWished ? R.color.leia_primary : R.color.leia_on_background));
        h.b.heart.setContentDescription(c.getString(isWished ? R.string.wish_remove : R.string.wish_add)
                + ": " + r.titulo);
        h.b.heart.setOnClickListener(v -> {
            v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(120)
                    .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()).start();
            listener.onToggleWish(r);
        });
        String walk = r.distancia == null ? "" : Labels.walk(c, r.distancia) + ". ";
        h.b.card.setContentDescription(c.getString(R.string.book_card_description, r.titulo, r.autor,
                (distance == null ? "" : distance + ", ") + walk + Labels.modality(c, r.modalidade), r.donoNome));
        h.b.card.setOnClickListener(v -> listener.onOpen(r));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemBookCardBinding b;

        Holder(ItemBookCardBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
