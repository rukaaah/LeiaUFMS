package br.ufms.leiaufms.ui.shelf;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.data.local.entity.ItemEstante;
import br.ufms.leiaufms.data.local.model.ShelfRow;
import br.ufms.leiaufms.databinding.ItemShelfBinding;
import br.ufms.leiaufms.ui.common.Covers;

/** Linha da estante (status) ou da lista de desejos (disponibilidade). */
public class ShelfAdapter extends ListAdapter<ShelfRow, ShelfAdapter.Holder> {

    public interface OnClick {
        void onClick(ShelfRow row);
    }

    private final OnClick onClick;

    public ShelfAdapter(OnClick onClick) {
        super(new DiffUtil.ItemCallback<ShelfRow>() {
            @Override
            public boolean areItemsTheSame(@NonNull ShelfRow a, @NonNull ShelfRow b) {
                return a.id == b.id && a.status.equals(b.status);
            }

            @Override
            public boolean areContentsTheSame(@NonNull ShelfRow a, @NonNull ShelfRow b) {
                return a.status.equals(b.status) && a.disponiveis == b.disponiveis;
            }
        });
        this.onClick = onClick;
    }

    public static String statusLabel(Context c, ShelfRow r) {
        switch (r.status) {
            case ItemEstante.STATUS_LENDO:
                return c.getString(R.string.shelf_reading);
            case ItemEstante.STATUS_LIDO:
                return c.getString(R.string.shelf_read);
            case ItemEstante.STATUS_PASSADO_ADIANTE:
                return c.getString(R.string.shelf_passed);
            default:
                return r.disponiveis > 0 ? c.getString(R.string.wish_available) : c.getString(R.string.wish_not_available);
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemShelfBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        ShelfRow r = getItem(position);
        Context c = h.itemView.getContext();
        h.b.title.setText(r.titulo);
        h.b.author.setText(r.autor);
        Covers.bind(h.b.cover.coverImage, h.b.cover.coverText, null, r.titulo);
        h.b.cover.coverText.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13);
        String status = statusLabel(c, r);
        h.b.status.setText(status);
        boolean wishAvailable = "DESEJO".equals(r.status) && r.disponiveis > 0;
        h.b.status.setTextColor(c.getColor(wishAvailable || !"DESEJO".equals(r.status)
                ? R.color.leia_primary : R.color.leia_on_surface_variant));
        h.itemView.setContentDescription(r.titulo + ", " + r.autor + ". " + status);
        boolean actionable = !ItemEstante.STATUS_PASSADO_ADIANTE.equals(r.status);
        h.itemView.setClickable(actionable);
        h.itemView.setOnClickListener(actionable ? v -> onClick.onClick(r) : null);
        h.b.status.setVisibility(View.VISIBLE);
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemShelfBinding b;

        Holder(ItemShelfBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
