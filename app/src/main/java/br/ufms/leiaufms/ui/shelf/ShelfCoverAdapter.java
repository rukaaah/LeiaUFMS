package br.ufms.leiaufms.ui.shelf;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.data.local.model.ShelfRow;
import br.ufms.leiaufms.databinding.ItemShelfCoverBinding;
import br.ufms.leiaufms.ui.common.Covers;

/** Estante pública em capas no Perfil (RF-35). */
public class ShelfCoverAdapter extends RecyclerView.Adapter<ShelfCoverAdapter.Holder> {

    private final List<ShelfRow> items = new ArrayList<>();

    public void submit(List<ShelfRow> rows) {
        items.clear();
        if (rows != null) items.addAll(rows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemShelfCoverBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        ShelfRow r = items.get(position);
        Context c = h.itemView.getContext();
        Covers.bind(h.b.cover.coverImage, h.b.cover.coverText, r.capaPath, r.titulo);
        String status = ShelfAdapter.statusLabel(c, r);
        h.b.status.setText(status);
        h.itemView.setContentDescription(c.getString(R.string.shelf_line, r.titulo, status));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemShelfCoverBinding b;

        Holder(ItemShelfCoverBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
