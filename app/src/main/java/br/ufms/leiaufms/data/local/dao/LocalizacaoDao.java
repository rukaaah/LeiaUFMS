package br.ufms.leiaufms.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import br.ufms.leiaufms.data.local.entity.LocalizacaoEncontro;

/** RF-41: posição compartilhada no encontro. */
@Dao
public interface LocalizacaoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(LocalizacaoEncontro l);

    @Query("DELETE FROM localizacao_encontro WHERE pedidoId = :pedidoId AND usuarioId = :usuarioId")
    void stop(long pedidoId, long usuarioId);

    @Query("DELETE FROM localizacao_encontro WHERE pedidoId = :pedidoId")
    void clearPedido(long pedidoId);

    @Query("SELECT * FROM localizacao_encontro WHERE pedidoId = :pedidoId AND usuarioId = :usuarioId")
    LiveData<LocalizacaoEncontro> observe(long pedidoId, long usuarioId);
}
