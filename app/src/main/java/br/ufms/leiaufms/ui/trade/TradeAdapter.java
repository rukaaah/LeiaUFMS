package br.ufms.leiaufms.ui.trade;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.data.local.model.PedidoRow;
import br.ufms.leiaufms.databinding.ItemTradeBinding;
import br.ufms.leiaufms.ui.common.Covers;
import br.ufms.leiaufms.ui.common.Labels;

/** Linha da aba Trocas: livro, outra parte, estado (texto + ícone) e próximo passo. */
public class TradeAdapter extends ListAdapter<PedidoRow, TradeAdapter.Holder> {

    public interface OnClick {
        void onClick(PedidoRow row);
    }

    private final long viewerId;
    private final OnClick onClick;

    public TradeAdapter(long viewerId, OnClick onClick) {
        super(new DiffUtil.ItemCallback<PedidoRow>() {
            @Override
            public boolean areItemsTheSame(@NonNull PedidoRow a, @NonNull PedidoRow b) {
                return a.pedido.id == b.pedido.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull PedidoRow a, @NonNull PedidoRow b) {
                return a.pedido.atualizadoEm == b.pedido.atualizadoEm && a.pedido.estado.equals(b.pedido.estado)
                        && a.pedido.confirmadoDono == b.pedido.confirmadoDono
                        && a.pedido.confirmadoSolicitante == b.pedido.confirmadoSolicitante;
            }
        });
        this.viewerId = viewerId;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemTradeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        PedidoRow r = getItem(position);
        Context c = h.itemView.getContext();
        h.b.title.setText(r.titulo);
        Covers.bind(h.b.cover.coverImage, h.b.cover.coverText, r.capaPath, r.titulo);
        boolean owner = r.pedido.donoId == viewerId;
        String party = owner ? c.getString(R.string.trade_with_requester, r.solicitanteNome)
                : c.getString(R.string.trade_with_owner, r.donoNome);
        h.b.party.setText(party);
        String state = Labels.state(c, r.pedido.estado);
        h.b.state.setText(state);
        h.b.state.setCompoundDrawablesRelativeWithIntrinsicBounds(Labels.stateIcon(r.pedido.estado), 0, 0, 0);
        String next = Labels.nextStep(c, r.pedido, viewerId);
        h.b.next.setVisibility(next == null ? View.GONE : View.VISIBLE);
        h.b.next.setText(next);
        h.itemView.setContentDescription(c.getString(R.string.trade_row_description, r.titulo, party, state,
                next == null ? "" : next));
        h.itemView.setOnClickListener(v -> onClick.onClick(r));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemTradeBinding b;

        Holder(ItemTradeBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
