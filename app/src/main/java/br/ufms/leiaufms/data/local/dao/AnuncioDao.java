package br.ufms.leiaufms.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import br.ufms.leiaufms.data.local.entity.Anuncio;
import br.ufms.leiaufms.data.local.model.ListingRow;

/** Anúncios (RF-12 a RF-14). */
@Dao
public interface AnuncioDao {

    String SELECT_ROW = "SELECT a.id AS anuncioId, a.modalidade, a.lat, a.lng, a.ativo, a.criadoEm, a.donoId, "
            + "a.pontoPreferidoId, e.id AS exemplarId, e.edicao, e.conservacao, e.fotoPath, o.id AS obraId, "
            + "o.titulo, o.autor, o.isbn13, o.capaPath, o.genero, u.nome AS donoNome, u.curso AS donoCurso, u.fotoPath AS donoFoto, "
            + "p.nome AS pontoNome, p.lat AS pontoLat, p.lng AS pontoLng "
            + "FROM anuncio a JOIN exemplar e ON e.id = a.exemplarId JOIN obra o ON o.id = e.obraId "
            + "JOIN usuario u ON u.id = a.donoId LEFT JOIN ponto_encontro p ON p.id = a.pontoPreferidoId ";

    @Insert
    long insert(Anuncio anuncio);

    @Update
    void update(Anuncio anuncio);

    @Query("SELECT * FROM anuncio WHERE id = :id")
    Anuncio findById(long id);

    /** Explorar: anúncios ativos sem pedido aceito, filtrados por texto e modalidade. */
    @Query(SELECT_ROW + "WHERE a.ativo = 1 "
            + "AND NOT EXISTS (SELECT 1 FROM pedido pd WHERE pd.anuncioId = a.id AND pd.estado IN ('ACEITO', 'EM_DISPUTA')) "
            + "AND (:q = '' OR o.titulo LIKE '%' || :q || '%' OR o.autor LIKE '%' || :q || '%' "
            + "OR REPLACE(COALESCE(o.isbn13, ''), '-', '') LIKE '%' || :q || '%') "
            + "AND (:modalidade = '' OR a.modalidade = :modalidade OR a.modalidade = 'AMBAS') "
            + "ORDER BY a.criadoEm DESC")
    LiveData<List<ListingRow>> search(String q, String modalidade);

    @Query(SELECT_ROW + "WHERE a.id = :id")
    LiveData<ListingRow> observeRow(long id);

    @Query(SELECT_ROW + "WHERE a.id = :id")
    ListingRow row(long id);

    @Query(SELECT_ROW + "WHERE a.ativo = 1 AND a.donoId = :usuarioId ORDER BY a.criadoEm DESC")
    LiveData<List<ListingRow>> byOwner(long usuarioId);

    @Query(SELECT_ROW + "WHERE a.ativo = 1 AND o.id = :obraId ORDER BY a.criadoEm DESC")
    LiveData<List<ListingRow>> byObra(long obraId);

    /** Início: livros indicados para disciplinas do curso do aluno (chave = primeira palavra do curso). */
    @Query(SELECT_ROW + "WHERE a.ativo = 1 AND a.donoId != :usuarioId "
            + "AND NOT EXISTS (SELECT 1 FROM pedido pd WHERE pd.anuncioId = a.id AND pd.estado IN ('ACEITO', 'EM_DISPUTA')) "
            + "AND (o.id IN (SELECT od.obraId FROM obra_disciplina od JOIN disciplina d ON d.id = od.disciplinaId "
            + "WHERE d.curso LIKE '%' || :chaveCurso || '%') "
            // RF-45: livros cujos tópicos aparecem nas disciplinas reais do curso
            + "OR o.id IN (SELECT ot.obraId FROM obra_topico ot JOIN disciplina_topico dt ON dt.topicoId = ot.topicoId "
            + "JOIN disciplina d ON d.id = dt.disciplinaId WHERE d.curso = :curso)) ORDER BY a.criadoEm DESC LIMIT 10")
    LiveData<List<ListingRow>> forCourse(long usuarioId, String chaveCurso, String curso);

    /** Explorar por gênero (grade de "Todos os livros"). */
    @Query(SELECT_ROW + "WHERE a.ativo = 1 "
            + "AND NOT EXISTS (SELECT 1 FROM pedido pd WHERE pd.anuncioId = a.id AND pd.estado IN ('ACEITO', 'EM_DISPUTA')) "
            + "AND (:q = '' OR o.titulo LIKE '%' || :q || '%' OR o.autor LIKE '%' || :q || '%' "
            + "OR REPLACE(COALESCE(o.isbn13, ''), '-', '') LIKE '%' || :q || '%') "
            + "AND (:genero = '' OR o.genero = :genero) ORDER BY a.criadoEm DESC")
    LiveData<List<ListingRow>> searchByGenre(String q, String genero);

    /** "Porque você leu X": mesmo autor ou mesmo gênero da obra lida, sem a própria obra. */
    @Query(SELECT_ROW + "WHERE a.ativo = 1 AND a.donoId != :me AND o.id != :obraId "
            + "AND (o.autor = (SELECT autor FROM obra WHERE id = :obraId) "
            + "OR (o.genero IS NOT NULL AND o.genero = (SELECT genero FROM obra WHERE id = :obraId))) "
            + "ORDER BY CASE WHEN o.autor = (SELECT autor FROM obra WHERE id = :obraId) THEN 0 ELSE 1 END, a.criadoEm DESC LIMIT 10")
    LiveData<List<ListingRow>> because(long me, long obraId);

    /** Gênero que a pessoa mais lê, deseja ou já trocou (estante + desejos + posses). */
    @Query("SELECT o.genero FROM obra o WHERE o.genero IS NOT NULL AND o.id IN ("
            + "SELECT obraId FROM item_estante WHERE usuarioId = :me UNION ALL "
            + "SELECT obraId FROM desejo WHERE usuarioId = :me UNION ALL "
            + "SELECT e.obraId FROM posse_exemplar p JOIN exemplar e ON e.id = p.exemplarId WHERE p.usuarioId = :me) "
            + "GROUP BY o.genero ORDER BY COUNT(*) DESC LIMIT 1")
    LiveData<String> topGenre(long me);

    @Query(SELECT_ROW + "WHERE a.ativo = 1 AND a.donoId != :me AND o.genero = :genero ORDER BY a.criadoEm DESC LIMIT 10")
    LiveData<List<ListingRow>> byGenre(long me, String genero);

    /** Aba Desejos: livros do mesmo autor ou das mesmas disciplinas dos desejos (sem repetir os desejados). */
    @Query(SELECT_ROW + "WHERE a.ativo = 1 AND a.donoId != :me "
            + "AND o.id NOT IN (SELECT obraId FROM desejo WHERE usuarioId = :me) "
            + "AND (o.autor IN (SELECT o2.autor FROM desejo d JOIN obra o2 ON o2.id = d.obraId WHERE d.usuarioId = :me) "
            + "OR o.id IN (SELECT od.obraId FROM obra_disciplina od WHERE od.disciplinaId IN "
            + "(SELECT od2.disciplinaId FROM obra_disciplina od2 JOIN desejo d2 ON d2.obraId = od2.obraId WHERE d2.usuarioId = :me))) "
            + "ORDER BY a.criadoEm DESC LIMIT 10")
    LiveData<List<ListingRow>> wishSuggestions(long me);

    @Query("SELECT COUNT(*) FROM anuncio a JOIN exemplar e ON e.id = a.exemplarId "
            + "WHERE e.obraId = :obraId AND a.ativo = 1 AND a.donoId != :exceptUserId")
    int activeCountForObra(long obraId, long exceptUserId);

    @Query("SELECT COUNT(*) FROM pedido WHERE anuncioId = :anuncioId AND estado IN ('ACEITO', 'EM_DISPUTA')")
    int countAccepted(long anuncioId);
}
