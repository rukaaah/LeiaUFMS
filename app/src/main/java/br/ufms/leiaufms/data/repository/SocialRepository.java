package br.ufms.leiaufms.data.repository;

import android.content.Context;

import androidx.annotation.WorkerThread;

import br.ufms.leiaufms.AppConfig;
import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.notification.Notifier;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.Avaliacao;
import br.ufms.leiaufms.data.local.entity.Desejo;
import br.ufms.leiaufms.data.local.entity.ItemEstante;
import br.ufms.leiaufms.data.local.entity.Mensagem;
import br.ufms.leiaufms.data.local.entity.Notificacao;
import br.ufms.leiaufms.data.local.entity.Obra;
import br.ufms.leiaufms.data.local.entity.Pedido;
import br.ufms.leiaufms.data.local.entity.Usuario;
import br.ufms.leiaufms.data.pedido.PedidoEstado;

/** Chat (RF-40), estante (RF-35, RF-36), desejos (RF-37) e avaliações (RF-25). */
public class SocialRepository {

    public enum Result { OK, NOT_ALLOWED, CLOSED, TOO_LONG, EMPTY, EXPIRED, DUPLICATE }

    private final Context context;
    private final AppDatabase db;

    public SocialRepository(Context context) {
        this.context = context.getApplicationContext();
        db = AppDatabase.get(context);
    }

    /** RF-40 / PC-12: só com pedido ACEITO, texto até 300 caracteres. */
    @WorkerThread
    public Result sendMessage(long pedidoId, long autorId, String texto, boolean pronta) {
        String t = texto == null ? "" : texto.trim();
        if (t.isEmpty()) return Result.EMPTY;
        if (t.length() > AppConfig.CHAT_MAX_CHARS) return Result.TOO_LONG;
        Pedido p = db.pedidoDao().findById(pedidoId);
        if (p == null || (p.donoId != autorId && p.solicitanteId != autorId)) return Result.NOT_ALLOWED;
        if (!PedidoEstado.ACEITO.equals(p.estado)) return Result.CLOSED;
        Mensagem m = new Mensagem();
        m.pedidoId = pedidoId;
        m.autorId = autorId;
        m.texto = t;
        m.pronta = pronta;
        m.enviadaEm = System.currentTimeMillis();
        db.socialDao().insertMensagem(m);
        long other = p.donoId == autorId ? p.solicitanteId : p.donoId;
        Usuario autor = db.usuarioDao().findById(autorId);
        Notifier.notify(context, other, Notifier.CHANNEL_TRADES,
                context.getString(R.string.notif_message_title, autor.nome), t, Notificacao.DESTINO_PEDIDO, pedidoId);
        return Result.OK;
    }

    /** Liga a uma obra existente por título + autor ou cria uma. */
    @WorkerThread
    public long obra(String titulo, String autor) {
        Obra o = db.obraDao().findByTitleAuthor(titulo.trim(), autor.trim());
        if (o != null) return o.id;
        Obra nova = new Obra();
        nova.titulo = titulo.trim();
        nova.autor = autor.trim();
        long id = db.obraDao().insert(nova);
        br.ufms.leiaufms.work.CoverWorker.enqueue(context);
        return id;
    }

    @WorkerThread
    public void addToShelf(long usuarioId, String titulo, String autor, String status) {
        ItemEstante i = new ItemEstante();
        i.usuarioId = usuarioId;
        i.obraId = obra(titulo, autor);
        i.status = status;
        i.atualizadoEm = System.currentTimeMillis();
        db.socialDao().upsertEstante(i);
    }

    @WorkerThread
    public void setShelfStatus(long itemId, long usuarioId, String status) {
        ItemEstante i = db.socialDao().shelfItem(itemId);
        if (i == null || i.usuarioId != usuarioId) return;
        i.status = status;
        i.atualizadoEm = System.currentTimeMillis();
        db.socialDao().upsertEstante(i);
    }

    @WorkerThread
    public void removeShelf(long itemId, long usuarioId) {
        db.socialDao().deleteShelf(itemId, usuarioId);
    }

    @WorkerThread
    public Result addWish(long usuarioId, String titulo, String autor) {
        long obraId = obra(titulo, autor);
        if (db.socialDao().hasWish(usuarioId, obraId) > 0) return Result.DUPLICATE;
        Desejo d = new Desejo();
        d.usuarioId = usuarioId;
        d.obraId = obraId;
        d.criadoEm = System.currentTimeMillis();
        db.socialDao().insertDesejo(d);
        return Result.OK;
    }

    /** Coração do card: liga ou desliga o desejo da obra. Devolve true se ficou desejada. */
    @WorkerThread
    public boolean toggleWish(long usuarioId, long obraId) {
        if (db.socialDao().hasWish(usuarioId, obraId) > 0) {
            db.socialDao().deleteWishByObra(usuarioId, obraId);
            return false;
        }
        Desejo d = new Desejo();
        d.usuarioId = usuarioId;
        d.obraId = obraId;
        d.criadoEm = System.currentTimeMillis();
        db.socialDao().insertDesejo(d);
        return true;
    }

    @WorkerThread
    public void removeWish(long id, long usuarioId) {
        db.socialDao().deleteWish(id, usuarioId);
    }

    /** RF-25 / PC-10: nota 1 a 5, uma por lado, editável por 48 h depois da conclusão. */
    @WorkerThread
    public Result rate(long pedidoId, long avaliadorId, int nota) {
        if (nota < 1 || nota > 5) return Result.NOT_ALLOWED;
        Pedido p = db.pedidoDao().findById(pedidoId);
        if (p == null || (p.donoId != avaliadorId && p.solicitanteId != avaliadorId)) return Result.NOT_ALLOWED;
        if (!PedidoEstado.CONCLUIDO.equals(p.estado)) return Result.CLOSED;
        Avaliacao existing = db.socialDao().rating(pedidoId, avaliadorId);
        long now = System.currentTimeMillis();
        if (existing != null && now - existing.criadaEm > AppConfig.RATING_EDIT_WINDOW_MS) return Result.EXPIRED;
        Avaliacao a = existing != null ? existing : new Avaliacao();
        a.pedidoId = pedidoId;
        a.avaliadorId = avaliadorId;
        a.avaliadoId = p.donoId == avaliadorId ? p.solicitanteId : p.donoId;
        a.nota = nota;
        if (existing == null) a.criadaEm = now;
        db.socialDao().upsertAvaliacao(a);
        return Result.OK;
    }

    @WorkerThread
    public Avaliacao myRating(long pedidoId, long avaliadorId) {
        return db.socialDao().rating(pedidoId, avaliadorId);
    }
}
