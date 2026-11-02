package br.ufms.leiaufms.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import br.ufms.leiaufms.data.local.entity.Sessao;

@Dao
public interface SessaoDao {

    @Insert
    long insert(Sessao sessao);

    @Query("SELECT * FROM sessao WHERE tokenHash = :tokenHash AND usuarioId = :usuarioId LIMIT 1")
    Sessao find(String tokenHash, long usuarioId);

    @Query("DELETE FROM sessao WHERE tokenHash = :tokenHash")
    void deleteByToken(String tokenHash);

    @Query("DELETE FROM sessao WHERE expiraEm < :now")
    void deleteExpired(long now);

    @Query("DELETE FROM sessao WHERE usuarioId = :usuarioId AND tokenHash != :keepTokenHash")
    void deleteOthers(long usuarioId, String keepTokenHash);
}
