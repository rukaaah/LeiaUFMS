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

    private static final String SEARCH = "https://openlibrary.org/search.json?limit=1&fields=cover_i";
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

    @Nullable
    Long searchCoverId(Obra o) throws IOException {
        String query = o.isbn13 != null && !o.isbn13.isEmpty()
                ? "&isbn=" + enc(o.isbn13)
                : "&title=" + enc(o.titulo) + "&author=" + enc(firstAuthor(o.autor));
        String json = new String(get(SEARCH + query), StandardCharsets.UTF_8);
        try {
            JSONArray docs = new JSONObject(json).optJSONArray("docs");
            if (docs == null || docs.length() == 0) return null;
            long id = docs.getJSONObject(0).optLong("cover_i", -1);
            return id > 0 ? id : null;
        } catch (org.json.JSONException e) {
            return null;
        }
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
