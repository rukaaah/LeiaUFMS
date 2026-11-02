package br.ufms.leiaufms.ui.inbox;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.data.local.entity.Notificacao;
import br.ufms.leiaufms.databinding.ItemNotificationBinding;
import br.ufms.leiaufms.ui.common.TimeText;

/** Linhas da Caixa de avisos. Não lido: ponto azul + "Não lido" para o TalkBack. */
public class NotificationAdapter extends ListAdapter<Notificacao, NotificationAdapter.Holder> {

    public interface OnClick {
        void onClick(Notificacao n);
    }

    private final OnClick onClick;

    public NotificationAdapter(OnClick onClick) {
        super(new DiffUtil.ItemCallback<Notificacao>() {
            @Override
            public boolean areItemsTheSame(@NonNull Notificacao a, @NonNull Notificacao b) {
                return a.id == b.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Notificacao a, @NonNull Notificacao b) {
                return a.lida == b.lida && a.titulo.equals(b.titulo) && a.texto.equals(b.texto);
            }
        });
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemNotificationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Notificacao n = getItem(position);
        h.b.title.setText(n.titulo);
        h.b.text.setText(n.texto);
        String when = TimeText.ago(h.itemView.getContext(), n.criadaEm);
        h.b.time.setText(when);
        h.b.dot.setVisibility(n.lida ? View.INVISIBLE : View.VISIBLE);
        String prefix = n.lida ? "" : h.itemView.getContext().getString(R.string.inbox_unread) + ". ";
        h.itemView.setContentDescription(prefix + n.titulo + ". " + n.texto + ". " + when);
        h.itemView.setOnClickListener(v -> onClick.onClick(n));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemNotificationBinding b;

        Holder(ItemNotificationBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
