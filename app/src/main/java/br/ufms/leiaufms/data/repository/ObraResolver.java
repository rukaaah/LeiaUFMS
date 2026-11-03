package br.ufms.leiaufms.data.repository;

import androidx.annotation.WorkerThread;

import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.CatalogLoader;
import br.ufms.leiaufms.data.local.entity.LivroCatalogo;
import br.ufms.leiaufms.data.local.entity.Obra;

/**
 * Acha ou cria a obra de um título + autor, padronizando pelo catálogo (RF-45): "calculo vol 1" digitado
 * sem acento vira o mesmo livro do catálogo, com título, autor, ISBN, gênero e tópicos certos.
 */
final class ObraResolver {

    private ObraResolver() {
    }

    @WorkerThread
    static long resolve(AppDatabase db, String titulo, String autor, String isbn, String genero) {
        String t = titulo.trim();
        String a = autor.trim();
        LivroCatalogo l = db.catalogoDao().byChave(CatalogLoader.key(t, a));
        if (l != null) {
            t = l.titulo;
            a = l.autor;
        }
        Obra o = null;
        if (isbn != null && !isbn.isEmpty()) o = db.obraDao().findByIsbn(isbn);
        if (o == null) o = db.obraDao().findByTitleAuthor(t, a);
        if (o != null) {
            boolean changed = false;
            if (o.isbn13 == null && isbn != null && !isbn.isEmpty()) {
                o.isbn13 = isbn;
                changed = true;
            }
            if (o.genero == null && genero != null) {
                o.genero = genero;
                changed = true;
            }
            if (changed) db.obraDao().update(o);
            if (l != null && o.catalogoId == null) CatalogLoader.link(db, o, l);
            return o.id;
        }
        Obra nova = new Obra();
        nova.titulo = t;
        nova.autor = a;
        nova.isbn13 = isbn == null || isbn.isEmpty() ? (l == null ? null : l.isbn13) : isbn;
        nova.genero = genero != null ? genero : (l == null ? null : l.genero);
        nova.id = db.obraDao().insert(nova);
        if (l != null) CatalogLoader.link(db, nova, l);
        return nova.id;
    }
}
