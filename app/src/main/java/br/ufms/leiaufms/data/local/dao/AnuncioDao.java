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
            + "o.titulo, o.autor, o.isbn13, o.capaPath, u.nome AS donoNome, u.curso AS donoCurso, u.fotoPath AS donoFoto, "
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
            + "AND o.id IN (SELECT od.obraId FROM obra_disciplina od JOIN disciplina d ON d.id = od.disciplinaId "
            + "WHERE d.curso LIKE '%' || :chaveCurso || '%') ORDER BY a.criadoEm DESC LIMIT 10")
    LiveData<List<ListingRow>> forCourse(long usuarioId, String chaveCurso);

    @Query("SELECT COUNT(*) FROM pedido WHERE anuncioId = :anuncioId AND estado IN ('ACEITO', 'EM_DISPUTA')")
    int countAccepted(long anuncioId);
}
