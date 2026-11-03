package br.ufms.leiaufms.ui.common;

import android.content.Context;

import java.util.Locale;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.location.WalkEstimate;
import br.ufms.leiaufms.data.local.entity.Anuncio;
import br.ufms.leiaufms.data.local.entity.Exemplar;
import br.ufms.leiaufms.data.local.entity.Pedido;
import br.ufms.leiaufms.data.pedido.PedidoEstado;

/** Textos de exibição de valores do banco (modalidade, conservação, estado, distância). */
public final class Labels {

    private Labels() {
    }

    public static String modality(Context c, String modalidade) {
        if (Anuncio.MODALIDADE_TROCA.equals(modalidade)) return c.getString(R.string.modality_trade);
        if (Anuncio.MODALIDADE_DOACAO.equals(modalidade)) return c.getString(R.string.modality_donation);
        return c.getString(R.string.modality_both);
    }

    public static String condition(Context c, String conservacao) {
        if (Exemplar.CONSERVACAO_NOVO.equals(conservacao)) return c.getString(R.string.condition_new);
        if (Exemplar.CONSERVACAO_BOM.equals(conservacao)) return c.getString(R.string.condition_good);
        if (Exemplar.CONSERVACAO_GASTO.equals(conservacao)) return c.getString(R.string.condition_worn);
        return c.getString(R.string.condition_used);
    }

    public static String distance(Context c, float meters) {
        if (meters < 1000) return c.getString(R.string.distance_m, Math.round(meters / 10f) * 10);
        return c.getString(R.string.distance_km, String.format(new Locale("pt", "BR"), "%.1f", meters / 1000f));
    }

    public static String walk(Context c, float meters) {
        return WalkEstimate.isWalkable(meters)
                ? c.getString(R.string.walk_yes, WalkEstimate.minutes(meters))
                : c.getString(R.string.walk_no);
    }

    public static String state(Context c, String estado) {
        switch (estado) {
            case PedidoEstado.PENDENTE:
                return c.getString(R.string.state_pending);
            case PedidoEstado.ACEITO:
                return c.getString(R.string.state_accepted);
            case PedidoEstado.RECUSADO:
                return c.getString(R.string.state_declined);
            case PedidoEstado.CANCELADO:
                return c.getString(R.string.state_cancelled);
            case PedidoEstado.EXPIRADO:
                return c.getString(R.string.state_expired);
            case PedidoEstado.CONCLUIDO:
                return c.getString(R.string.state_concluded);
            default:
                return c.getString(R.string.state_dispute);
        }
    }

    public static int stateIcon(String estado) {
        switch (estado) {
            case PedidoEstado.PENDENTE:
                return R.drawable.ic_schedule;
            case PedidoEstado.ACEITO:
                return R.drawable.ic_swap;
            case PedidoEstado.CONCLUIDO:
                return R.drawable.ic_check_circle;
            case PedidoEstado.EM_DISPUTA:
                return R.drawable.ic_gavel;
            default:
                return R.drawable.ic_close;
        }
    }

    /** Próximo passo do pedido para quem está olhando (null quando não há). */
    public static String nextStep(Context c, Pedido p, long viewerId) {
        boolean owner = p.donoId == viewerId;
        switch (p.estado) {
            case PedidoEstado.PENDENTE:
                return c.getString(owner ? R.string.next_answer : R.string.next_wait_answer);
            case PedidoEstado.ACEITO:
                if (p.encontroEm == null) return c.getString(R.string.next_schedule);
                if (!p.encontroAceito) {
                    return c.getString(proposedBy(p, viewerId) ? R.string.next_wait_meeting : R.string.next_accept_meeting);
                }
                boolean mine = owner ? p.confirmadoDono : p.confirmadoSolicitante;
                return c.getString(mine ? R.string.next_wait_other : R.string.next_confirm);
            case PedidoEstado.EM_DISPUTA:
                return c.getString(R.string.next_dispute);
            default:
                return null;
        }
    }

    /** Pedido em que o usuário precisa agir agora (vai para o topo da lista). */
    public static boolean needsAction(Pedido p, long viewerId) {
        if (PedidoEstado.PENDENTE.equals(p.estado)) return p.donoId == viewerId;
        if (PedidoEstado.ACEITO.equals(p.estado)) {
            boolean mine = p.donoId == viewerId ? p.confirmadoDono : p.confirmadoSolicitante;
            if (p.encontroEm == null) return true;
            if (!p.encontroAceito) return !proposedBy(p, viewerId);
            return !mine;
        }
        return false;
    }

    /** RF-44: quem está olhando foi quem propôs o horário atual. */
    public static boolean proposedBy(Pedido p, long viewerId) {
        return p.encontroPropostoPor != null && p.encontroPropostoPor == viewerId;
    }

    public static boolean isRescue(Pedido p) {
        return Pedido.TIPO_RESGATE.equals(p.tipo);
    }
}
