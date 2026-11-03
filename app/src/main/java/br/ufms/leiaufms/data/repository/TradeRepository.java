package br.ufms.leiaufms.data.repository;

import android.content.Context;

import androidx.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.List;

import br.ufms.leiaufms.AppConfig;
import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.notification.Notifier;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.Anuncio;
import br.ufms.leiaufms.data.local.entity.Exemplar;
import br.ufms.leiaufms.data.local.entity.ItemEstante;
import br.ufms.leiaufms.data.local.entity.Mensagem;
import br.ufms.leiaufms.data.local.entity.MovimentoCredito;
import br.ufms.leiaufms.data.local.entity.Notificacao;
import br.ufms.leiaufms.data.local.entity.Obra;
import br.ufms.leiaufms.data.local.entity.Pedido;
import br.ufms.leiaufms.data.local.entity.PontoEncontro;
import br.ufms.leiaufms.data.local.entity.PosseExemplar;
import br.ufms.leiaufms.data.local.entity.Usuario;
import br.ufms.leiaufms.data.pedido.Badges;
import br.ufms.leiaufms.data.pedido.CreditRules;
import br.ufms.leiaufms.data.pedido.PedidoEstado;
import br.ufms.leiaufms.ui.common.TimeText;

/**
 * Pedidos, trocas e créditos (RF-18 a RF-24, RN-TROCA-*, RN-CRED-*). Toda mudança de estado roda numa
 * transação Room e passa pela tabela de transições de {@link PedidoEstado}. As notificações são
 * disparadas depois da transação.
 */
public class TradeRepository {

    public enum Result {
        OK, NOT_FOUND, CLOSED, OWN_LISTING, ALREADY_REQUESTED, LIMIT, WRONG_TYPE, OFFER_UNAVAILABLE,
        NOT_ALLOWED, INVALID_STATE, NO_POINT, BAD_DATE
    }

    /** Aviso a ser enviado depois do commit da transação. */
    private static final class Pending {
        final long usuarioId;
        final String canal;
        final String titulo;
        final String texto;
        final long pedidoId;

        Pending(long usuarioId, String canal, String titulo, String texto, long pedidoId) {
            this.usuarioId = usuarioId;
            this.canal = canal;
            this.titulo = titulo;
            this.texto = texto;
            this.pedidoId = pedidoId;
        }
    }

    private final Context context;
    private final AppDatabase db;

    public TradeRepository(Context context) {
        this.context = context.getApplicationContext();
        db = AppDatabase.get(context);
    }

    // RF-18 / RF-19
    @WorkerThread
    public Result request(long anuncioId, long usuarioId, String tipo, Long exemplarOferecidoId, long[] outId) {
        List<Pending> out = new ArrayList<>();
        Result r = db.runInTransaction(() -> {
            Anuncio a = db.anuncioDao().findById(anuncioId);
            if (a == null || !a.ativo || db.anuncioDao().countAccepted(anuncioId) > 0) return Result.CLOSED;
            if (a.donoId == usuarioId) return Result.OWN_LISTING; // RN-TROCA-01
            if (db.pedidoDao().activeFor(anuncioId, usuarioId) != null) return Result.ALREADY_REQUESTED;
            if (db.pedidoDao().countPendingBy(usuarioId) >= AppConfig.MAX_PENDING_REQUESTS) return Result.LIMIT;
            boolean troca = Pedido.TIPO_TROCA_DIRETA.equals(tipo);
            if ((troca && !a.aceitaTroca()) || (!troca && !a.aceitaDoacao())) return Result.WRONG_TYPE;
            if (troca) {
                Exemplar oferecido = exemplarOferecidoId == null ? null : db.exemplarDao().findById(exemplarOferecidoId);
                if (oferecido == null || oferecido.donoAtualId != usuarioId
                        || db.pedidoDao().countOfferAccepted(oferecido.id) > 0) return Result.OFFER_UNAVAILABLE;
            }
            int saldo = db.creditoDao().available(usuarioId);
            long now = System.currentTimeMillis();
            Pedido p = new Pedido();
            p.anuncioId = anuncioId;
            p.solicitanteId = usuarioId;
            p.donoId = a.donoId;
            p.tipo = tipo;
            p.exemplarOferecidoId = troca ? exemplarOferecidoId : null;
            p.estado = PedidoEstado.PENDENTE;
            p.semCredito = CreditRules.isWithoutCredit(tipo, saldo);
            p.criadoEm = now;
            p.atualizadoEm = now;
            p.id = db.pedidoDao().insert(p);
            int reserva = CreditRules.reserveOnRequest(tipo, saldo);
            if (reserva != 0) credit(usuarioId, reserva, MovimentoCredito.TIPO_RESERVA, p.id);
            outId[0] = p.id;

            Usuario sol = db.usuarioDao().findById(usuarioId);
            String livro = titulo(a);
            String texto;
            if (troca) {
                texto = context.getString(R.string.notif_new_trade_text, sol.nome, livro);
            } else if (p.semCredito) {
                texto = context.getString(R.string.notif_new_rescue_no_credit_text, sol.nome, livro);
            } else {
                texto = context.getString(R.string.notif_new_rescue_text, sol.nome, livro, saldo);
            }
            out.add(new Pending(a.donoId, Notifier.CHANNEL_TRADES,
                    context.getString(R.string.notif_new_request_title, livro), texto, p.id));
            return Result.OK;
        });
        send(out);
        return r;
    }

    // RF-20
    @WorkerThread
    public Result accept(long pedidoId, long usuarioId) {
        List<Pending> out = new ArrayList<>();
        Result r = db.runInTransaction(() -> {
            Pedido p = db.pedidoDao().findById(pedidoId);
            if (p == null) return Result.NOT_FOUND;
            if (p.donoId != usuarioId) return Result.NOT_ALLOWED;
            if (!PedidoEstado.canTransition(p.estado, PedidoEstado.ACEITO)) return Result.INVALID_STATE;
            long now = System.currentTimeMillis();
            move(p, PedidoEstado.ACEITO, now);
            p.aceitoEm = now;
            db.pedidoDao().update(p);
            String livro = tituloPedido(p);
            out.add(new Pending(p.solicitanteId, Notifier.CHANNEL_TRADES,
                    context.getString(R.string.notif_accepted_title),
                    context.getString(R.string.notif_accepted_text, nome(p.donoId), livro), p.id));
            // Recusa automática dos outros pedidos do mesmo anúncio, liberando reservas
            for (Pedido other : db.pedidoDao().otherPending(p.anuncioId, p.id)) {
                move(other, PedidoEstado.RECUSADO, now);
                other.motivo = context.getString(R.string.reason_other_accepted);
                db.pedidoDao().update(other);
                release(other);
                out.add(new Pending(other.solicitanteId, Notifier.CHANNEL_TRADES,
                        context.getString(R.string.notif_declined_title),
                        context.getString(R.string.notif_declined_other_text, livro), other.id));
            }
            return Result.OK;
        });
        send(out);
        return r;
    }

    @WorkerThread
    public Result decline(long pedidoId, long usuarioId) {
        List<Pending> out = new ArrayList<>();
        Result r = db.runInTransaction(() -> {
            Pedido p = db.pedidoDao().findById(pedidoId);
            if (p == null) return Result.NOT_FOUND;
            if (p.donoId != usuarioId) return Result.NOT_ALLOWED;
            if (!PedidoEstado.canTransition(p.estado, PedidoEstado.RECUSADO)) return Result.INVALID_STATE;
            move(p, PedidoEstado.RECUSADO, System.currentTimeMillis());
            db.pedidoDao().update(p);
            release(p);
            out.add(new Pending(p.solicitanteId, Notifier.CHANNEL_TRADES,
                    context.getString(R.string.notif_declined_title),
                    context.getString(R.string.notif_declined_text, nome(p.donoId), tituloPedido(p)), p.id));
            return Result.OK;
        });
        send(out);
        return r;
    }

    // RF-23
    @WorkerThread
    public Result cancel(long pedidoId, long usuarioId, String motivo) {
        List<Pending> out = new ArrayList<>();
        Result r = db.runInTransaction(() -> {
            Pedido p = db.pedidoDao().findById(pedidoId);
            if (p == null) return Result.NOT_FOUND;
            if (p.donoId != usuarioId && p.solicitanteId != usuarioId) return Result.NOT_ALLOWED;
            if (PedidoEstado.EM_DISPUTA.equals(p.estado)
                    || !PedidoEstado.canTransition(p.estado, PedidoEstado.CANCELADO)) return Result.INVALID_STATE;
            move(p, PedidoEstado.CANCELADO, System.currentTimeMillis());
            p.motivo = motivo;
            db.pedidoDao().update(p);
            release(p);
            long other = p.donoId == usuarioId ? p.solicitanteId : p.donoId;
            String texto = motivo == null || motivo.trim().isEmpty()
                    ? context.getString(R.string.notif_cancelled_text, nome(usuarioId), tituloPedido(p))
                    : context.getString(R.string.notif_cancelled_reason_text, nome(usuarioId), tituloPedido(p), motivo.trim());
            out.add(new Pending(other, Notifier.CHANNEL_TRADES,
                    context.getString(R.string.notif_cancelled_title), texto, p.id));
            return Result.OK;
        });
        send(out);
        return r;
    }

    // RF-21
    @WorkerThread
    public Result scheduleMeeting(long pedidoId, long usuarioId, long pontoId, long quando) {
        List<Pending> out = new ArrayList<>();
        Result r = db.runInTransaction(() -> {
            Pedido p = db.pedidoDao().findById(pedidoId);
            if (p == null) return Result.NOT_FOUND;
            if (p.donoId != usuarioId && p.solicitanteId != usuarioId) return Result.NOT_ALLOWED;
            if (!PedidoEstado.ACEITO.equals(p.estado)) return Result.INVALID_STATE;
            PontoEncontro ponto = db.pontoDao().findById(pontoId);
            if (ponto == null || !ponto.ativo) return Result.NO_POINT;
            long now = System.currentTimeMillis();
            long base = p.aceitoEm == null ? p.atualizadoEm : p.aceitoEm;
            if (quando <= now || quando > base + AppConfig.DELIVERY_DEADLINE_MS) return Result.BAD_DATE;
            p.pontoEncontroId = pontoId;
            p.encontroEm = quando;
            p.lembreteEncontroEnviado = false;
            p.atualizadoEm = now;
            db.pedidoDao().update(p);
            String quandoTexto = TimeText.dateTime(quando);
            Mensagem m = new Mensagem();
            m.pedidoId = p.id;
            m.autorId = usuarioId;
            m.texto = context.getString(R.string.chat_meeting_auto, ponto.nome, quandoTexto);
            m.enviadaEm = now;
            db.socialDao().insertMensagem(m);
            long other = p.donoId == usuarioId ? p.solicitanteId : p.donoId;
            out.add(new Pending(other, Notifier.CHANNEL_TRADES,
                    context.getString(R.string.notif_meeting_title),
                    context.getString(R.string.notif_meeting_text, nome(usuarioId), ponto.nome, quandoTexto), p.id));
            return Result.OK;
        });
        send(out);
        return r;
    }

    // RF-22
    @WorkerThread
    public Result confirm(long pedidoId, long usuarioId) {
        List<Pending> out = new ArrayList<>();
        boolean[] concludedNow = {false};
        Result r = db.runInTransaction(() -> {
            Pedido p = db.pedidoDao().findById(pedidoId);
            if (p == null) return Result.NOT_FOUND;
            if (!PedidoEstado.ACEITO.equals(p.estado)) return Result.INVALID_STATE;
            if (p.donoId == usuarioId) p.confirmadoDono = true;
            else if (p.solicitanteId == usuarioId) p.confirmadoSolicitante = true;
            else return Result.NOT_ALLOWED;
            long now = System.currentTimeMillis();
            p.atualizadoEm = now;
            long other = p.donoId == usuarioId ? p.solicitanteId : p.donoId;
            String livro = tituloPedido(p);
            if (p.confirmadoDono && p.confirmadoSolicitante) {
                conclude(p, now);
                String titulo = context.getString(R.string.notif_concluded_title);
                out.add(new Pending(p.donoId, Notifier.CHANNEL_TRADES, titulo,
                        context.getString(R.string.notif_concluded_text, livro), p.id));
                out.add(new Pending(p.solicitanteId, Notifier.CHANNEL_TRADES, titulo,
                        context.getString(R.string.notif_concluded_text, livro), p.id));
                concludedNow[0] = true;
            } else {
                db.pedidoDao().update(p);
                out.add(new Pending(other, Notifier.CHANNEL_TRADES,
                        context.getString(R.string.notif_confirm_other_title),
                        context.getString(R.string.notif_confirm_other_text, nome(usuarioId), livro), p.id));
            }
            return Result.OK;
        });
        send(out);
        if (concludedNow[0]) checkBadgesFor(pedidoId);
        return r;
    }

    // RF-26: qualquer parte abre disputa num pedido aceito; créditos e exemplares ficam congelados
    @WorkerThread
    public Result openDispute(long pedidoId, long usuarioId, String motivo) {
        List<Pending> out = new ArrayList<>();
        Result r = db.runInTransaction(() -> {
            Pedido p = db.pedidoDao().findById(pedidoId);
            if (p == null) return Result.NOT_FOUND;
            if (p.donoId != usuarioId && p.solicitanteId != usuarioId) return Result.NOT_ALLOWED;
            if (!PedidoEstado.canTransition(p.estado, PedidoEstado.EM_DISPUTA)) return Result.INVALID_STATE;
            move(p, PedidoEstado.EM_DISPUTA, System.currentTimeMillis());
            p.motivo = motivo;
            db.pedidoDao().update(p);
            long other = p.donoId == usuarioId ? p.solicitanteId : p.donoId;
            String livro = tituloPedido(p);
            out.add(new Pending(other, Notifier.CHANNEL_TRADES, context.getString(R.string.notif_dispute_title),
                    context.getString(R.string.notif_dispute_text, nome(usuarioId), livro), p.id));
            for (Usuario m : db.usuarioDao().moderators()) {
                out.add(new Pending(m.id, Notifier.CHANNEL_PENDING, context.getString(R.string.notif_dispute_mod_title),
                        context.getString(R.string.notif_dispute_mod_text, livro), p.id));
            }
            return Result.OK;
        });
        send(out);
        return r;
    }

    /** RF-26: o Moderador decide: conclui (efeitos de conclusão) ou cancela (libera a reserva). */
    @WorkerThread
    public Result resolveDispute(long pedidoId, long moderadorId, boolean concluir) {
        List<Pending> out = new ArrayList<>();
        Result r = db.runInTransaction(() -> {
            Usuario mod = db.usuarioDao().findById(moderadorId);
            if (mod == null || !mod.isModerador()) return Result.NOT_ALLOWED;
            Pedido p = db.pedidoDao().findById(pedidoId);
            if (p == null) return Result.NOT_FOUND;
            if (!PedidoEstado.EM_DISPUTA.equals(p.estado)) return Result.INVALID_STATE;
            long now = System.currentTimeMillis();
            if (concluir) {
                p.confirmadoDono = true;
                p.confirmadoSolicitante = true;
                conclude(p, now);
            } else {
                move(p, PedidoEstado.CANCELADO, now);
                db.pedidoDao().update(p);
                release(p);
            }
            String texto = context.getString(concluir ? R.string.notif_dispute_done : R.string.notif_dispute_cancel,
                    tituloPedido(p));
            out.add(new Pending(p.donoId, Notifier.CHANNEL_TRADES, context.getString(R.string.notif_dispute_resolved), texto, p.id));
            out.add(new Pending(p.solicitanteId, Notifier.CHANNEL_TRADES, context.getString(R.string.notif_dispute_resolved), texto, p.id));
            return Result.OK;
        });
        send(out);
        if (r == Result.OK && concluir) checkBadgesFor(pedidoId);
        return r;
    }

    /** RF-11 / PC-09: avisa quem acabou de ganhar um selo. */
    @WorkerThread
    public void checkBadgesFor(long pedidoId) {
        Pedido p = db.pedidoDao().findById(pedidoId);
        if (p == null || !PedidoEstado.CONCLUIDO.equals(p.estado)) return;
        for (long u : new long[]{p.donoId, p.solicitanteId}) {
            int doacoes = db.pedidoDao().countDonations(u);
            int trocas = db.pedidoDao().countConcluded(u);
            if (Badges.justEarnedDonor(doacoes, p, u)) {
                Notifier.notify(context, u, Notifier.CHANNEL_TRADES, context.getString(R.string.notif_badge_title),
                        context.getString(R.string.notif_badge_donor), Notificacao.DESTINO_PERFIL, u);
            }
            if (trocas == AppConfig.BADGE_VETERAN_MIN) {
                Notifier.notify(context, u, Notifier.CHANNEL_TRADES, context.getString(R.string.notif_badge_title),
                        context.getString(R.string.notif_badge_veteran), Notificacao.DESTINO_PERFIL, u);
            }
        }
    }

    /** Troca de posse, histórico, créditos e estante. Chamado dentro da transação. */
    void conclude(Pedido p, long now) {
        move(p, PedidoEstado.CONCLUIDO, now);
        db.pedidoDao().update(p);
        Anuncio a = db.anuncioDao().findById(p.anuncioId);
        a.ativo = false;
        db.anuncioDao().update(a);
        transfer(a.exemplarId, p.solicitanteId, p.id, now);
        if (p.exemplarOferecidoId != null) transfer(p.exemplarOferecidoId, p.donoId, p.id, now);
        // A reserva do resgatador (se houver) vira o consumo definitivo; o doador ganha +1
        int donor = CreditRules.donorCreditOnConclusion(p.tipo);
        if (donor != 0) credit(p.donoId, donor, MovimentoCredito.TIPO_DOACAO, p.id);
        Exemplar e = db.exemplarDao().findById(a.exemplarId);
        shelf(p.donoId, e.obraId, ItemEstante.STATUS_PASSADO_ADIANTE, now);
        if (p.exemplarOferecidoId != null) {
            Exemplar oferecido = db.exemplarDao().findById(p.exemplarOferecidoId);
            shelf(p.solicitanteId, oferecido.obraId, ItemEstante.STATUS_PASSADO_ADIANTE, now);
        }
    }

    /** RF-24: expiração (72 h), cancelamento por prazo (7 dias) e lembretes (RF-33). */
    @WorkerThread
    public void runDeadlines(long now) {
        List<Pending> out = new ArrayList<>();
        db.runInTransaction(() -> {
            for (Pedido p : db.pedidoDao().pendingOlderThan(now - AppConfig.REQUEST_EXPIRATION_MS)) {
                move(p, PedidoEstado.EXPIRADO, now);
                db.pedidoDao().update(p);
                release(p);
                String livro = tituloPedido(p);
                out.add(new Pending(p.solicitanteId, Notifier.CHANNEL_PENDING,
                        context.getString(R.string.notif_expired_title),
                        context.getString(R.string.notif_expired_text_requester, livro), p.id));
                out.add(new Pending(p.donoId, Notifier.CHANNEL_PENDING,
                        context.getString(R.string.notif_expired_title),
                        context.getString(R.string.notif_expired_text_owner, livro), p.id));
            }
            for (Pedido p : db.pedidoDao().acceptedOlderThan(now - AppConfig.DELIVERY_DEADLINE_MS)) {
                move(p, PedidoEstado.CANCELADO, now);
                p.motivo = context.getString(R.string.reason_deadline);
                db.pedidoDao().update(p);
                release(p);
                String texto = context.getString(R.string.notif_deadline_cancel_text, tituloPedido(p));
                out.add(new Pending(p.solicitanteId, Notifier.CHANNEL_PENDING,
                        context.getString(R.string.notif_cancelled_title), texto, p.id));
                out.add(new Pending(p.donoId, Notifier.CHANNEL_PENDING,
                        context.getString(R.string.notif_cancelled_title), texto, p.id));
            }
            for (Pedido p : db.pedidoDao().needingDeadlineReminder(now - AppConfig.DELIVERY_REMINDER_MS)) {
                p.lembreteEnviado = true;
                db.pedidoDao().update(p);
                String texto = context.getString(R.string.notif_deadline_reminder_text, tituloPedido(p));
                out.add(new Pending(p.solicitanteId, Notifier.CHANNEL_PENDING,
                        context.getString(R.string.notif_deadline_reminder_title), texto, p.id));
                out.add(new Pending(p.donoId, Notifier.CHANNEL_PENDING,
                        context.getString(R.string.notif_deadline_reminder_title), texto, p.id));
            }
            for (Pedido p : db.pedidoDao().meetingsSoon(now, now + 24L * 60 * 60 * 1000)) {
                p.lembreteEncontroEnviado = true;
                db.pedidoDao().update(p);
                PontoEncontro ponto = p.pontoEncontroId == null ? null : db.pontoDao().findById(p.pontoEncontroId);
                String texto = context.getString(R.string.notif_meeting_soon_text, tituloPedido(p),
                        ponto == null ? "" : ponto.nome, TimeText.dateTime(p.encontroEm));
                out.add(new Pending(p.solicitanteId, Notifier.CHANNEL_PENDING,
                        context.getString(R.string.notif_meeting_soon_title), texto, p.id));
                out.add(new Pending(p.donoId, Notifier.CHANNEL_PENDING,
                        context.getString(R.string.notif_meeting_soon_title), texto, p.id));
            }
        });
        send(out);
    }

    // ---- auxiliares (dentro da transação) ----

    private void move(Pedido p, String to, long now) {
        PedidoEstado.requireTransition(p.estado, to);
        p.estado = to;
        p.atualizadoEm = now;
        // RF-41: encerrado o pedido, nenhuma posição compartilhada continua guardada
        if (!PedidoEstado.ACEITO.equals(to)) db.localizacaoDao().clearPedido(p.id);
    }

    private void release(Pedido p) {
        int amount = CreditRules.releaseAmount(db.creditoDao().netReserve(p.id, p.solicitanteId));
        if (amount > 0) credit(p.solicitanteId, amount, MovimentoCredito.TIPO_LIBERACAO, p.id);
    }

    private void credit(long usuarioId, int valor, String tipo, long pedidoId) {
        MovimentoCredito m = new MovimentoCredito();
        m.usuarioId = usuarioId;
        m.valor = valor;
        m.tipo = tipo;
        m.pedidoId = pedidoId;
        m.criadoEm = System.currentTimeMillis();
        db.creditoDao().insert(m);
    }

    private void transfer(long exemplarId, long novoDono, long pedidoId, long now) {
        Exemplar e = db.exemplarDao().findById(exemplarId);
        e.donoAtualId = novoDono;
        db.exemplarDao().update(e);
        db.exemplarDao().closeCurrentPossession(exemplarId, now);
        PosseExemplar posse = new PosseExemplar();
        posse.exemplarId = exemplarId;
        posse.usuarioId = novoDono;
        posse.desde = now;
        posse.pedidoId = pedidoId;
        db.exemplarDao().insertPosse(posse);
    }

    private void shelf(long usuarioId, long obraId, String status, long now) {
        ItemEstante item = new ItemEstante();
        item.usuarioId = usuarioId;
        item.obraId = obraId;
        item.status = status;
        item.atualizadoEm = now;
        db.socialDao().upsertEstante(item);
    }

    private String titulo(Anuncio a) {
        Exemplar e = db.exemplarDao().findById(a.exemplarId);
        Obra o = e == null ? null : db.obraDao().findById(e.obraId);
        return o == null ? "" : o.titulo;
    }

    private String tituloPedido(Pedido p) {
        Anuncio a = db.anuncioDao().findById(p.anuncioId);
        return a == null ? "" : titulo(a);
    }

    private String nome(long usuarioId) {
        Usuario u = db.usuarioDao().findById(usuarioId);
        return u == null ? "" : u.nome;
    }

    private void send(List<Pending> out) {
        for (Pending n : out) {
            Notifier.notify(context, n.usuarioId, n.canal, n.titulo, n.texto, Notificacao.DESTINO_PEDIDO, n.pedidoId);
        }
    }
}
