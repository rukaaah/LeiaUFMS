package br.ufms.leiaufms.data.repository;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ufms.leiaufms.R;
import br.ufms.leiaufms.core.media.ImageStore;
import br.ufms.leiaufms.core.notification.Notifier;
import br.ufms.leiaufms.core.util.Validators;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.Anuncio;
import br.ufms.leiaufms.data.local.entity.Exemplar;
import br.ufms.leiaufms.data.local.entity.Notificacao;
import br.ufms.leiaufms.data.local.entity.Obra;
import br.ufms.leiaufms.data.local.entity.PosseExemplar;

/** Anúncios (RF-12, RF-13) e aviso para quem deseja a obra (RF-32). */
public class ListingRepository {

    public enum Result { OK, BLOCKED, NOT_FOUND, NOT_ALLOWED }

    /** Dados do formulário de anúncio. */
    public static class Form {
        public String titulo;
        public String autor;
        public String isbn;
        public String edicao;
        public String conservacao;
        public String modalidade;
        @Nullable
        public String genero;
        @Nullable
        public Uri foto;
        @Nullable
        public Double lat;
        @Nullable
        public Double lng;
        @Nullable
        public Long pontoId;
    }

    private final Context context;
    private final AppDatabase db;

    public ListingRepository(Context context) {
        this.context = context.getApplicationContext();
        db = AppDatabase.get(context);
    }

    /** RF-12: liga a uma obra existente (ISBN ou título + autor) ou cria uma; cria exemplar e anúncio. */
    @WorkerThread
    public long create(long donoId, Form f) throws IOException {
        String foto = f.foto == null ? null : ImageStore.save(context, f.foto, "exemplar_" + donoId);
        List<Long> interessados = new ArrayList<>();
        long[] obraOut = new long[1];
        long id = db.runInTransaction(() -> {
            long now = System.currentTimeMillis();
            long obraId = findOrCreateObra(f.titulo, f.autor, f.isbn, f.genero);
            obraOut[0] = obraId;
            Exemplar e = new Exemplar();
            e.obraId = obraId;
            e.donoAtualId = donoId;
            e.edicao = emptyToNull(f.edicao);
            e.conservacao = f.conservacao;
            e.fotoPath = foto;
            e.id = db.exemplarDao().insert(e);
            PosseExemplar posse = new PosseExemplar();
            posse.exemplarId = e.id;
            posse.usuarioId = donoId;
            posse.desde = now;
            db.exemplarDao().insertPosse(posse);
            Anuncio a = new Anuncio();
            a.exemplarId = e.id;
            a.donoId = donoId;
            a.modalidade = f.modalidade;
            a.lat = f.lat;
            a.lng = f.lng;
            a.pontoPreferidoId = f.pontoId;
            a.ativo = true;
            a.criadoEm = now;
            long anuncioId = db.anuncioDao().insert(a);
            interessados.addAll(db.socialDao().wishersOf(obraId, donoId));
            return anuncioId;
        });
        br.ufms.leiaufms.work.CoverWorker.enqueue(context);
        // RF-32: avisa quem tem a obra na lista de desejos
        Obra obra = db.obraDao().findById(obraOut[0]);
        for (Long usuarioId : interessados) {
            Notifier.notify(context, usuarioId, Notifier.CHANNEL_WISHES,
                    context.getString(R.string.notif_wish_title),
                    context.getString(R.string.notif_wish_text, obra.titulo), Notificacao.DESTINO_ANUNCIO, id);
        }
        return id;
    }

    /** RF-13: editar. Bloqueado se houver pedido aceito. */
    @WorkerThread
    public Result update(long anuncioId, long donoId, Form f) throws IOException {
        Anuncio a = db.anuncioDao().findById(anuncioId);
        if (a == null) return Result.NOT_FOUND;
        if (a.donoId != donoId) return Result.NOT_ALLOWED;
        if (db.anuncioDao().countAccepted(anuncioId) > 0) return Result.BLOCKED;
        String foto = f.foto == null ? null : ImageStore.save(context, f.foto, "exemplar_" + donoId);
        return db.runInTransaction(() -> {
            Exemplar e = db.exemplarDao().findById(a.exemplarId);
            e.obraId = findOrCreateObra(f.titulo, f.autor, f.isbn, f.genero);
            e.edicao = emptyToNull(f.edicao);
            e.conservacao = f.conservacao;
            if (foto != null) {
                ImageStore.delete(context, e.fotoPath);
                e.fotoPath = foto;
            }
            db.exemplarDao().update(e);
            a.modalidade = f.modalidade;
            a.lat = f.lat;
            a.lng = f.lng;
            a.pontoPreferidoId = f.pontoId;
            db.anuncioDao().update(a);
            return Result.OK;
        });
    }

    /** RF-13: encerrar. Pedidos pendentes do anúncio são recusados (com liberação de reserva). */
    @WorkerThread
    public Result close(long anuncioId, long donoId) {
        Anuncio a = db.anuncioDao().findById(anuncioId);
        if (a == null) return Result.NOT_FOUND;
        if (a.donoId != donoId) return Result.NOT_ALLOWED;
        if (db.anuncioDao().countAccepted(anuncioId) > 0) return Result.BLOCKED;
        TradeRepository trades = new TradeRepository(context);
        for (br.ufms.leiaufms.data.local.entity.Pedido p : db.pedidoDao().otherPending(anuncioId, -1)) {
            trades.decline(p.id, donoId);
        }
        a.ativo = false;
        db.anuncioDao().update(a);
        return Result.OK;
    }

    private long findOrCreateObra(String titulo, String autor, String isbnRaw, String genero) {
        String isbn = Validators.normalizeIsbn(isbnRaw);
        Obra o = null;
        if (!isbn.isEmpty()) o = db.obraDao().findByIsbn(isbn);
        if (o == null) o = db.obraDao().findByTitleAuthor(titulo.trim(), autor.trim());
        if (o != null) {
            boolean changed = false;
            if (o.isbn13 == null && !isbn.isEmpty()) {
                o.isbn13 = isbn;
                changed = true;
            }
            if (o.genero == null && genero != null) {
                o.genero = genero;
                changed = true;
            }
            if (changed) db.obraDao().update(o);
            return o.id;
        }
        Obra nova = new Obra();
        nova.titulo = titulo.trim();
        nova.autor = autor.trim();
        nova.isbn13 = isbn.isEmpty() ? null : isbn;
        nova.genero = genero;
        return db.obraDao().insert(nova);
    }

    private static String emptyToNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }
}
