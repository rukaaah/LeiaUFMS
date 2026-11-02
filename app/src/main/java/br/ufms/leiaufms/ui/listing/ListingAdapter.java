package br.ufms.leiaufms.ui.listing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.location.WalkEstimate;
import br.ufms.leiaufms.data.local.model.ListingRow;
import br.ufms.leiaufms.databinding.ItemListingBinding;
import br.ufms.leiaufms.ui.common.Covers;
import br.ufms.leiaufms.ui.common.Labels;

/** Card de anúncio (03-design-system §5). Lido pelo TalkBack como um item só. */
public class ListingAdapter extends ListAdapter<ListingRow, ListingAdapter.Holder> {

    public interface OnClick {
        void onClick(ListingRow row);
    }

    private final OnClick onClick;

    public ListingAdapter(OnClick onClick) {
        super(new DiffUtil.ItemCallback<ListingRow>() {
            @Override
            public boolean areItemsTheSame(@NonNull ListingRow a, @NonNull ListingRow b) {
                return a.anuncioId == b.anuncioId;
            }

            @Override
            public boolean areContentsTheSame(@NonNull ListingRow a, @NonNull ListingRow b) {
                return a.titulo.equals(b.titulo) && a.modalidade.equals(b.modalidade)
                        && java.util.Objects.equals(a.distancia, b.distancia)
                        && java.util.Objects.equals(a.fotoPath, b.fotoPath);
            }
        });
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemListingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        ListingRow r = getItem(position);
        Context c = h.itemView.getContext();
        h.b.title.setText(r.titulo);
        h.b.author.setText(r.autor);
        Covers.bind(h.b.cover.coverImage, h.b.cover.coverText, r.fotoPath, r.titulo);
        String distance;
        if (r.distancia != null) {
            distance = Labels.distance(c, r.distancia) + " · " + Labels.walk(c, r.distancia);
            h.b.distance.setText(distance);
            h.b.distance.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    WalkEstimate.isWalkable(r.distancia) ? R.drawable.ic_walk_small : 0, 0, 0, 0);
            h.b.distance.setVisibility(View.VISIBLE);
        } else {
            distance = "";
            h.b.distance.setVisibility(View.GONE);
        }
        String meta = Labels.modality(c, r.modalidade) + " · " + Labels.condition(c, r.conservacao) + " · " + r.donoNome;
        h.b.meta.setText(meta);
        h.itemView.setContentDescription(c.getString(R.string.listing_card_description, r.titulo, r.autor,
                distance.isEmpty() ? c.getString(R.string.no_distance) : distance,
                Labels.modality(c, r.modalidade), r.donoNome));
        h.itemView.setOnClickListener(v -> onClick.onClick(r));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemListingBinding b;

        Holder(ItemListingBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
