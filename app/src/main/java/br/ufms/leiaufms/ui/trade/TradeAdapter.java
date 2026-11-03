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
        String party = owner ? r.solicitanteNome : r.donoNome;
        h.b.party.setText(party);
        boolean hasMeeting = r.pedido.encontroEm != null && r.pontoNome != null
                && br.ufms.leiaufms.data.pedido.PedidoEstado.ACEITO.equals(r.pedido.estado);
        h.b.where.setVisibility(hasMeeting ? View.VISIBLE : View.GONE);
        h.b.when.setVisibility(hasMeeting ? View.VISIBLE : View.GONE);
        String whenText = "";
        if (hasMeeting) {
            h.b.where.setText(r.pontoNome);
            whenText = br.ufms.leiaufms.ui.common.TimeText.meeting(c, r.pedido.encontroEm);
            h.b.when.setText(whenText);
        }
        String state = Labels.state(c, r.pedido.estado);
        h.b.state.setText(state);
        h.b.state.setCompoundDrawablesRelativeWithIntrinsicBounds(Labels.stateIcon(r.pedido.estado), 0, 0, 0);
        int step = br.ufms.leiaufms.ui.common.TradeProgressView.stepOf(r.pedido);
        boolean active = step >= 0 && step < 3;
        h.b.state.setVisibility(active ? View.GONE : View.VISIBLE);
        h.b.progress.setVisibility(active ? View.VISIBLE : View.GONE);
        if (active) h.b.progress.setStep(step, false);
        boolean needs = Labels.needsAction(r.pedido, viewerId);
        String next = Labels.nextStep(c, r.pedido, viewerId);
        h.b.next.setVisibility(needs && next != null ? View.VISIBLE : View.GONE);
        h.b.next.setText(next);
        h.b.card.setStrokeColor(c.getColor(needs ? R.color.leia_primary : R.color.leia_outline_variant));
        h.b.card.setStrokeWidth(needs ? Math.round(2 * c.getResources().getDisplayMetrics().density) : 0);
        h.itemView.setContentDescription(c.getString(R.string.trade_row_description, r.titulo, party,
                hasMeeting ? r.pontoNome + ", " + whenText : state, next == null ? "" : next));
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
