package br.ufms.leiaufms.data.local;

import android.content.Context;

import androidx.annotation.WorkerThread;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.ufms.leiaufms.data.local.entity.CatalogoTopico;
import br.ufms.leiaufms.data.local.entity.Disciplina;
import br.ufms.leiaufms.data.local.entity.DisciplinaTopico;
import br.ufms.leiaufms.data.local.entity.LivroCatalogo;
import br.ufms.leiaufms.data.local.entity.Obra;
import br.ufms.leiaufms.data.local.entity.ObraTopico;
import br.ufms.leiaufms.data.local.entity.Topico;

/**
 * Carrega assets/catalogo.json (gerado por tools/catalogo/gerar_catalogo.py) na primeira abertura:
 * livros da Open Library, tópicos e as disciplinas reais da UFMS (RF-45). Roda uma vez, em background.
 */
public final class CatalogLoader {

    private CatalogLoader() {
    }

    /** "Cálculo – Vol. 1" + "James Stewart" → "calculo – vol. 1|james stewart" (sem acento, minúsculas). */
    public static String key(String titulo, String autor) {
        return plain(titulo) + "|" + plain(autor);
    }

    public static String plain(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private static final String PREFS = "catalogo";
    private static final String KEY_VERSION = "versao";

    /** Carrega na primeira vez e recarrega quando o catalogo.json do app muda de versão. */
    @WorkerThread
    public static void loadIfNeeded(Context context, AppDatabase db) {
        android.content.SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        try (InputStream in = context.getAssets().open("catalogo.json")) {
            byte[] bytes = new byte[in.available()];
            int off = 0;
            while (off < bytes.length) {
                int r = in.read(bytes, off, bytes.length - off);
                if (r < 0) break;
                off += r;
            }
            JSONObject root = new JSONObject(new String(bytes, 0, off, StandardCharsets.UTF_8));
            String version = root.optString("versao", "1");
            if (version.equals(prefs.getString(KEY_VERSION, null)) && db.catalogoDao().topicCount() > 0) return;
            db.runInTransaction(() -> {
                try {
                    clear(db);
                    load(db, root);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
            prefs.edit().putString(KEY_VERSION, version).apply();
        } catch (Exception e) {
            android.util.Log.w("CatalogLoader", "Catálogo não carregado", e);
        }
    }

    private static void clear(AppDatabase db) {
        db.catalogoDao().clearObraTopicos();
        db.catalogoDao().clearCatalogoTopicos();
        db.catalogoDao().clearDisciplinaTopicos();
        db.catalogoDao().clearDisciplinasDoCatalogo();
        db.catalogoDao().clearLivros();
        db.catalogoDao().clearTopicos();
        db.catalogoDao().unlinkObras();
    }

    private static void load(AppDatabase db, JSONObject root) throws Exception {
        JSONArray tps = root.getJSONArray("topicos");
        List<Topico> topicos = new ArrayList<>();
        for (int i = 0; i < tps.length(); i++) {
            Topico t = new Topico();
            t.id = i + 1;
            t.nome = tps.getJSONObject(i).getString("n");
            t.genero = tps.getJSONObject(i).getString("g");
            topicos.add(t);
        }
        db.catalogoDao().insertTopicos(topicos);

        JSONArray livros = root.getJSONArray("livros");
        List<LivroCatalogo> ls = new ArrayList<>();
        List<CatalogoTopico> lt = new ArrayList<>();
        for (int i = 0; i < livros.length(); i++) {
            JSONObject o = livros.getJSONObject(i);
            LivroCatalogo l = new LivroCatalogo();
            l.id = i + 1;
            l.titulo = o.getString("t");
            l.autor = o.getString("a");
            l.chave = key(l.titulo, l.autor);
            l.isbn13 = o.isNull("i") ? null : o.getString("i");
            l.ano = o.isNull("y") ? null : o.getInt("y");
            l.capaOl = o.isNull("c") ? null : o.getLong("c");
            l.genero = o.getString("g");
            ls.add(l);
            JSONArray tp = o.getJSONArray("tp");
            for (int k = 0; k < tp.length(); k++) {
                CatalogoTopico c = new CatalogoTopico();
                c.livroId = l.id;
                c.topicoId = tp.getInt(k) + 1;
                lt.add(c);
            }
        }
        db.catalogoDao().insertLivros(ls);
        db.catalogoDao().insertCatalogoTopicos(lt);

        JSONArray ds = root.getJSONArray("disciplinas");
        List<Disciplina> disc = new ArrayList<>();
        for (int i = 0; i < ds.length(); i++) {
            JSONObject o = ds.getJSONObject(i);
            Disciplina d = new Disciplina();
            d.codigo = o.getString("c");
            d.nome = o.getString("n");
            d.curso = o.getString("cu");
            disc.add(d);
        }
        List<Long> ids = db.catalogoDao().insertDisciplinas(disc);
        List<DisciplinaTopico> dt = new ArrayList<>();
        for (int i = 0; i < ds.length(); i++) {
            JSONArray tp = ds.getJSONObject(i).getJSONArray("tp");
            for (int k = 0; k < tp.length(); k++) {
                DisciplinaTopico x = new DisciplinaTopico();
                x.disciplinaId = ids.get(i);
                x.topicoId = tp.getInt(k) + 1;
                dt.add(x);
            }
        }
        db.catalogoDao().insertDisciplinaTopicos(dt);

        // Obras que já existiam (seed, anúncios antigos) ganham os tópicos do livro do catálogo correspondente
        for (Obra o : db.obraDao().all()) {
            LivroCatalogo l = db.catalogoDao().byChave(key(o.titulo, o.autor));
            if (l != null) link(db, o, l);
        }
    }

    /** Liga a obra ao livro do catálogo: guarda a origem e copia os tópicos. */
    @WorkerThread
    public static void link(AppDatabase db, Obra o, LivroCatalogo l) {
        o.catalogoId = l.id;
        if (o.isbn13 == null) o.isbn13 = l.isbn13;
        if (o.genero == null) o.genero = l.genero;
        db.obraDao().update(o);
        List<ObraTopico> ot = new ArrayList<>();
        for (long t : db.catalogoDao().topicsOfLivro(l.id)) {
            ObraTopico x = new ObraTopico();
            x.obraId = o.id;
            x.topicoId = t;
            ot.add(x);
        }
        db.catalogoDao().insertObraTopicos(ot);
    }
}
