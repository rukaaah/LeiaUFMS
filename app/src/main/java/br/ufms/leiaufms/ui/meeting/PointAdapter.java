package br.ufms.leiaufms.ui.meeting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.data.local.entity.PontoEncontro;
import br.ufms.leiaufms.databinding.ItemPointBinding;

/** Lista de pontos seguros: alternativa ao mapa (sem internet e para o TalkBack). */
public class PointAdapter extends RecyclerView.Adapter<PointAdapter.Holder> {

    public interface Listener {
        void onSelect(PontoEncontro p);

        void onRemove(PontoEncontro p);
    }

    private final List<PontoEncontro> items = new ArrayList<>();
    private final boolean manage;
    private final Listener listener;
    private long selectedId = -1;

    public PointAdapter(boolean manage, Listener listener) {
        this.manage = manage;
        this.listener = listener;
    }

    public void submit(List<PontoEncontro> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public void select(long id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemPointBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        PontoEncontro p = items.get(position);
        h.b.name.setText(p.nome);
        h.b.description.setText(p.descricao);
        h.b.description.setVisibility(p.descricao == null ? View.GONE : View.VISIBLE);
        h.b.radio.setVisibility(manage ? View.GONE : View.VISIBLE);
        h.b.radio.setChecked(p.id == selectedId);
        h.b.remove.setVisibility(manage ? View.VISIBLE : View.GONE);
        h.b.remove.setContentDescription(h.itemView.getContext().getString(R.string.action_remove) + " " + p.nome);
        h.b.remove.setOnClickListener(v -> listener.onRemove(p));
        if (manage) {
            h.itemView.setOnClickListener(v -> listener.onSelect(p));
            h.itemView.setSelected(false);
        } else {
            h.itemView.setOnClickListener(v -> listener.onSelect(p));
            h.itemView.setSelected(p.id == selectedId);
            androidx.core.view.ViewCompat.setStateDescription(h.itemView,
                    p.id == selectedId ? h.itemView.getContext().getString(R.string.state_selected) : null);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemPointBinding b;

        Holder(ItemPointBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
