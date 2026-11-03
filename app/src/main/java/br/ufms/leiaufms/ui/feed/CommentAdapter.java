package br.ufms.leiaufms.ui.feed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import br.ufms.leiaufms.data.local.model.MessageRow;
import br.ufms.leiaufms.databinding.ItemCommentBinding;
import br.ufms.leiaufms.ui.common.ImageLoader;
import br.ufms.leiaufms.ui.common.TimeText;

/** Comentários de um post do mural (RF-43), do mais antigo para o mais novo. */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.Holder> {

    private final List<MessageRow> items = new ArrayList<>();

    public void submit(List<MessageRow> rows) {
        items.clear();
        if (rows != null) items.addAll(rows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemCommentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        MessageRow m = items.get(position);
        Context c = h.itemView.getContext();
        String when = TimeText.ago(c, m.enviadaEm);
        h.b.initials.setText(ImageLoader.initials(m.autorNome));
        h.b.meta.setText(m.autorNome + " · " + when);
        h.b.text.setText(m.texto);
        h.b.row.setContentDescription(m.autorNome + ", " + when + ": " + m.texto);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemCommentBinding b;

        Holder(ItemCommentBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
