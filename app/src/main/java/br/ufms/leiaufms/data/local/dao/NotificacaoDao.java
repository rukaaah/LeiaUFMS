package br.ufms.leiaufms.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import br.ufms.leiaufms.data.local.entity.Notificacao;

@Dao
public interface NotificacaoDao {

    @Insert
    long insert(Notificacao notificacao);

    @Query("SELECT * FROM notificacao WHERE usuarioId = :usuarioId ORDER BY criadaEm DESC")
    LiveData<List<Notificacao>> observeAll(long usuarioId);

    @Query("SELECT COUNT(*) FROM notificacao WHERE usuarioId = :usuarioId AND lida = 0")
    LiveData<Integer> observeUnread(long usuarioId);

    @Query("SELECT * FROM notificacao WHERE usuarioId = :usuarioId AND entregue = 0 ORDER BY criadaEm")
    List<Notificacao> undelivered(long usuarioId);

    @Query("UPDATE notificacao SET entregue = 1 WHERE usuarioId = :usuarioId")
    void markDelivered(long usuarioId);

    @Query("UPDATE notificacao SET lida = 1 WHERE id = :id")
    void markRead(long id);

    @Query("UPDATE notificacao SET lida = 1 WHERE usuarioId = :usuarioId")
    void markAllRead(long usuarioId);
}
