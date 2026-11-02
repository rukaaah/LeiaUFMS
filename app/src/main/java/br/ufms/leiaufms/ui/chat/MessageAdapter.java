package br.ufms.leiaufms.ui.chat;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.data.local.model.MessageRow;
import br.ufms.leiaufms.databinding.ItemMessageBinding;
import br.ufms.leiaufms.ui.common.TimeText;

/** Balões do chat: as minhas à direita (azul-claro), as da outra parte à esquerda. */
public class MessageAdapter extends ListAdapter<MessageRow, MessageAdapter.Holder> {

    private final long me;

    public MessageAdapter(long me) {
        super(new DiffUtil.ItemCallback<MessageRow>() {
            @Override
            public boolean areItemsTheSame(@NonNull MessageRow a, @NonNull MessageRow b) {
                return a.id == b.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull MessageRow a, @NonNull MessageRow b) {
                return a.texto.equals(b.texto);
            }
        });
        this.me = me;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        MessageRow m = getItem(position);
        Context c = h.itemView.getContext();
        boolean mine = m.autorId == me;
        h.b.row.setGravity(mine ? Gravity.END : Gravity.START);
        h.b.bubble.setBackgroundResource(mine ? R.drawable.bg_bubble_mine : R.drawable.bg_bubble_other);
        h.b.bubble.setText(m.texto);
        String when = TimeText.dateTime(m.enviadaEm);
        h.b.time.setText(when);
        ((LinearLayout.LayoutParams) h.b.bubble.getLayoutParams()).gravity = mine ? Gravity.END : Gravity.START;
        h.itemView.setContentDescription(c.getString(R.string.chat_bubble_description,
                mine ? c.getString(R.string.chat_me) : m.autorNome, when, m.texto));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemMessageBinding b;

        Holder(ItemMessageBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
