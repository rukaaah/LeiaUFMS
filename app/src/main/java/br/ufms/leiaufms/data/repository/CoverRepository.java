package br.ufms.leiaufms.data.repository;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.Obra;

/**
 * Capas reais dos livros (decisão do Pedro em 03/11/2026, RF-42). Consome a API pública da
 * Open Library: busca por título + autor (ou ISBN, quando houver), baixa a capa e guarda em
 * filesDir/capas. Só o título e o autor saem do aparelho; nenhum dado pessoal é enviado.
 */
public class CoverRepository {

    private static final String SEARCH = "https://openlibrary.org/search.json?limit=5&fields=cover_i";
    private static final String COVER = "https://covers.openlibrary.org/b/id/%d-M.jpg?default=false";
    private static final int TIMEOUT_MS = 8000;
    private static final long RETRY_MS = 24L * 60 * 60 * 1000;

    private final Context context;
    private final AppDatabase db;

    public CoverRepository(Context context) {
        this.context = context.getApplicationContext();
        db = AppDatabase.get(context);
    }

    /** Busca as capas que faltam. Devolve quantas foram baixadas. */
    @WorkerThread
    public int fetchMissing() {
        long now = System.currentTimeMillis();
        List<Obra> obras = db.obraDao().withoutCover(now - RETRY_MS);
        int ok = 0;
        for (Obra o : obras) {
            String path = null;
            try {
                Long coverId = searchCoverId(o);
                if (coverId != null) path = download(coverId, o.id);
            } catch (IOException | RuntimeException ignored) {
                // Sem internet ou resposta inesperada: tenta de novo depois de RETRY_MS
            }
            db.obraDao().setCover(o.id, path, now);
            if (path != null) ok++;
        }
        return ok;
    }

    /**
     * Tenta, em ordem: ISBN; título curto (antes de ":" ou ",") + sobrenome do primeiro autor;
     * título completo + primeiro autor. Fica com o primeiro resultado que tenha capa.
     */
    @Nullable
    Long searchCoverId(Obra o) throws IOException {
        // Obra vinda do catálogo já sabe o id da capa na Open Library (RF-45): sem busca
        if (o.catalogoId != null) {
            br.ufms.leiaufms.data.local.entity.LivroCatalogo l = db.catalogoDao().byId(o.catalogoId);
            if (l != null && l.capaOl != null) return l.capaOl;
        }
        if (o.isbn13 != null && !o.isbn13.isEmpty()) {
            Long id = firstCover("&isbn=" + enc(o.isbn13));
            if (id != null) return id;
        }
        String author = firstAuthor(o.autor);
        String[] parts = author.split("\\s+");
        String lastName = parts[parts.length - 1];
        Long id = firstCover("&title=" + enc(shortTitle(o.titulo)) + "&author=" + enc(lastName));
        if (id != null) return id;
        return firstCover("&title=" + enc(o.titulo) + "&author=" + enc(author));
    }

    @Nullable
    private Long firstCover(String query) throws IOException {
        String json = new String(get(SEARCH + query), StandardCharsets.UTF_8);
        try {
            JSONArray docs = new JSONObject(json).optJSONArray("docs");
            if (docs == null) return null;
            for (int i = 0; i < docs.length(); i++) {
                long id = docs.getJSONObject(i).optLong("cover_i", -1);
                if (id > 0) return id;
            }
            return null;
        } catch (org.json.JSONException e) {
            return null;
        }
    }

    static String shortTitle(String titulo) {
        String t = titulo == null ? "" : titulo;
        int cut = t.length();
        for (char ch : new char[]{':', ','}) {
            int i = t.indexOf(ch);
            if (i > 0 && i < cut) cut = i;
        }
        return t.substring(0, cut).trim();
    }

    @Nullable
    private String download(long coverId, long obraId) throws IOException {
        byte[] data = get(String.format(java.util.Locale.ROOT, COVER, coverId));
        if (data.length < 1000) return null; // imagem vazia/placeholder
        File dir = new File(context.getFilesDir(), "capas");
        if (!dir.exists()) dir.mkdirs();
        File out = new File(dir, "obra_" + obraId + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(data);
        }
        return out.getAbsolutePath();
    }

    /**
     * Capa de um livro do catálogo (RF-45), guardada em filesDir/capas/cat_{id}.jpg. Devolve o caminho
     * ou null sem internet. Só o id da capa sai do aparelho.
     */
    @WorkerThread
    @Nullable
    public String catalogCover(long livroId, long capaOl) {
        File out = new File(new File(context.getFilesDir(), "capas"), "cat_" + livroId + ".jpg");
        if (out.exists()) return out.getAbsolutePath();
        try {
            byte[] data = get(String.format(java.util.Locale.ROOT, COVER, capaOl));
            if (data.length < 1000) return null;
            out.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(out)) {
                fos.write(data);
            }
            return out.getAbsolutePath();
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    private static byte[] get(String url) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(TIMEOUT_MS);
        c.setReadTimeout(TIMEOUT_MS);
        c.setInstanceFollowRedirects(true);
        c.setRequestProperty("User-Agent", "LeiaUFMS/1.0 (app academico UFMS)");
        try {
            int code = c.getResponseCode();
            if (code / 100 != 2) throw new IOException("HTTP " + code);
            try (InputStream in = c.getInputStream(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) bos.write(buf, 0, n);
                return bos.toByteArray();
            }
        } finally {
            c.disconnect();
        }
    }

    private static String firstAuthor(String autor) {
        if (autor == null) return "";
        String a = autor.split(",| e ")[0];
        return a.trim();
    }

    private static String enc(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return s;
        }
    }
}
