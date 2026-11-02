package br.ufms.leiaufms.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import br.ufms.leiaufms.data.local.entity.Exemplar;
import br.ufms.leiaufms.data.local.entity.PosseExemplar;

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

    @Query("UPDATE posse_exemplar SET ate = :ate WHERE exemplarId = :exemplarId AND ate IS NULL")
    void closeCurrentPossession(long exemplarId, long ate);

    @Query("SELECT * FROM posse_exemplar WHERE exemplarId = :exemplarId ORDER BY desde")
    List<PosseExemplar> history(long exemplarId);
}
