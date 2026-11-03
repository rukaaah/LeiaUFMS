package br.ufms.leiaufms.data.repository;

import androidx.annotation.WorkerThread;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Busca na Open Library quando o catálogo do app não tem o livro (feedback 03/11: "Percy Jackson",
 * "O Brasil como problema"). Só o texto digitado sai do aparelho. Sem internet, devolve lista vazia.
 */
public final class OpenLibrarySearch {

    public static final class Result {
        public final String titulo;
        public final String autor;
        public final Integer ano;
        public final String isbn13;
        public final Long capa;

        Result(String titulo, String autor, Integer ano, String isbn13, Long capa) {
            this.titulo = titulo;
            this.autor = autor;
            this.ano = ano;
            this.isbn13 = isbn13;
            this.capa = capa;
        }
    }

    /** Pede a edição em português de cada obra: "The Lightning Thief" vira "O ladrão de raios". */
    private static final String URL_SEARCH = "https://openlibrary.org/search.json?limit=15"
            + "&fields=key,title,author_name,first_publish_year,isbn,cover_i,editions,editions.key,editions.title,editions.isbn,editions.cover_i";  // "key" é necessário para a Open Library devolver as edições

    private OpenLibrarySearch() {
    }

    @WorkerThread
    public static List<Result> search(String q) {
        List<Result> pt = search(q, "&language=por");
        return pt.isEmpty() ? search(q, "") : pt;
    }

    @WorkerThread
    private static List<Result> search(String q, String filter) {
        List<Result> out = new ArrayList<>();
        HttpURLConnection c = null;
        try {
            c = (HttpURLConnection) new URL(URL_SEARCH + filter + "&q=" + URLEncoder.encode(q.trim(), "UTF-8")).openConnection();
            c.setConnectTimeout(4000);
            c.setReadTimeout(5000);
            c.setRequestProperty("User-Agent", "LeiaUFMS/1.0 (app academico UFMS)");
            if (c.getResponseCode() / 100 != 2) return out;
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            try (InputStream in = c.getInputStream()) {
                byte[] b = new byte[8192];
                int n;
                while ((n = in.read(b)) > 0) buf.write(b, 0, n);
            }
            JSONArray docs = new JSONObject(buf.toString(StandardCharsets.UTF_8.name())).optJSONArray("docs");
            if (docs == null) return out;
            for (int i = 0; i < docs.length(); i++) {
                JSONObject d = docs.getJSONObject(i);
                JSONArray authors = d.optJSONArray("author_name");
                String title = d.optString("title", "").trim();
                if (title.isEmpty() || authors == null || authors.length() == 0) continue;
                JSONObject ed = null;
                JSONObject eds = d.optJSONObject("editions");
                if (eds != null && eds.optJSONArray("docs") != null && eds.optJSONArray("docs").length() > 0) {
                    ed = eds.optJSONArray("docs").optJSONObject(0);
                    if (ed != null && !ed.optString("title", "").trim().isEmpty()) title = ed.optString("title").trim();
                }
                String isbn = null;
                JSONArray isbns = ed != null && ed.optJSONArray("isbn") != null ? ed.optJSONArray("isbn") : d.optJSONArray("isbn");
                if (isbns != null) {
                    for (int k = 0; k < isbns.length(); k++) {
                        String s = isbns.optString(k);
                        if (s.length() == 13 && (s.startsWith("978") || s.startsWith("979"))) {
                            isbn = s;
                            break;
                        }
                    }
                }
                out.add(new Result(title, authors.optString(0).trim(),
                        d.has("first_publish_year") ? d.optInt("first_publish_year") : null, isbn,
                        ed != null && ed.has("cover_i") ? Long.valueOf(ed.optLong("cover_i"))
                                : d.has("cover_i") ? Long.valueOf(d.optLong("cover_i")) : null));
            }
        } catch (Exception ignored) {
            // Sem internet ou resposta inesperada: ficam só as sugestões locais
        } finally {
            if (c != null) c.disconnect();
        }
        return out;
    }
}
