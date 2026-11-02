package br.ufms.leiaufms.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import br.ufms.leiaufms.data.local.entity.PontoEncontro;

/** Pontos seguros (RF-29, RF-30). Só o Moderador cria; nenhum ponto vem no seed. */
@Dao
public interface PontoDao {

    @Insert
    long insert(PontoEncontro ponto);

    @Query("SELECT * FROM ponto_encontro WHERE ativo = 1 ORDER BY nome COLLATE NOCASE")
    List<PontoEncontro> active();

    @Query("SELECT * FROM ponto_encontro WHERE ativo = 1 ORDER BY nome COLLATE NOCASE")
    androidx.lifecycle.LiveData<List<PontoEncontro>> observeActive();

    @Query("UPDATE ponto_encontro SET ativo = 0 WHERE id = :id")
    void deactivate(long id);

    @Query("SELECT * FROM ponto_encontro WHERE id = :id")
    PontoEncontro findById(long id);
}
