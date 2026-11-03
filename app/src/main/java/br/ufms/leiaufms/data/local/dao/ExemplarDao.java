package br.ufms.leiaufms.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import br.ufms.leiaufms.data.local.entity.Exemplar;
import br.ufms.leiaufms.data.local.entity.PosseExemplar;
import br.ufms.leiaufms.data.local.model.ExemplarRow;

@Dao
public interface ExemplarDao {

    @Insert
    long insert(Exemplar exemplar);

    @Update
    void update(Exemplar exemplar);

    @Insert
    long insertPosse(PosseExemplar posse);

    @Query("SELECT * FROM exemplar WHERE id = :id")
    Exemplar findById(long id);

    @Query("SELECT * FROM exemplar WHERE donoAtualId = :usuarioId")
    List<Exemplar> byOwner(long usuarioId);

    @Query("SELECT e.id AS exemplarId, o.id AS obraId, o.titulo, o.autor, e.edicao FROM exemplar e "
            + "JOIN obra o ON o.id = e.obraId WHERE e.donoAtualId = :usuarioId "
            + "AND NOT EXISTS (SELECT 1 FROM pedido p WHERE p.exemplarOferecidoId = e.id AND p.estado IN ('ACEITO', 'EM_DISPUTA')) "
            + "ORDER BY o.titulo COLLATE NOCASE")
    List<ExemplarRow> offerable(long usuarioId);

    @Query("UPDATE posse_exemplar SET ate = :ate WHERE exemplarId = :exemplarId AND ate IS NULL")
    void closeCurrentPossession(long exemplarId, long ate);

    /** RF-17: por quantas mãos o exemplar passou. */
    @Query("SELECT COUNT(DISTINCT usuarioId) FROM posse_exemplar WHERE exemplarId = :exemplarId")
    int hands(long exemplarId);

    @Query("SELECT * FROM posse_exemplar WHERE exemplarId = :exemplarId ORDER BY desde")
    List<PosseExemplar> history(long exemplarId);
}
