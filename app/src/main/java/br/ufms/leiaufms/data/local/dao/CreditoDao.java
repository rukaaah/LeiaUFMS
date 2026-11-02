package br.ufms.leiaufms.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import br.ufms.leiaufms.data.local.entity.MovimentoCredito;

/** Livro-razão de créditos (ADR-05, RN-CRED-01). */
@Dao
public interface CreditoDao {

    @Insert
    long insert(MovimentoCredito movimento);

    /** Saldo disponível: soma de todos os lançamentos (reservas já entram como -1). */
    @Query("SELECT COALESCE(SUM(valor), 0) FROM movimento_credito WHERE usuarioId = :usuarioId")
    int available(long usuarioId);

    @Query("SELECT COALESCE(SUM(valor), 0) FROM movimento_credito WHERE usuarioId = :usuarioId")
    LiveData<Integer> observeAvailable(long usuarioId);

    /** Créditos presos em pedidos ainda ativos (reserva menos liberação). */
    @Query("SELECT COALESCE(-SUM(m.valor), 0) FROM movimento_credito m JOIN pedido p ON p.id = m.pedidoId "
            + "WHERE m.usuarioId = :usuarioId AND m.tipo IN ('RESERVA', 'LIBERACAO') "
            + "AND p.estado IN ('PENDENTE', 'ACEITO', 'EM_DISPUTA')")
    int reserved(long usuarioId);

    @Query("SELECT * FROM movimento_credito WHERE pedidoId = :pedidoId")
    List<MovimentoCredito> byPedido(long pedidoId);

    @Query("SELECT * FROM movimento_credito WHERE usuarioId = :usuarioId ORDER BY criadoEm DESC")
    List<MovimentoCredito> byUser(long usuarioId);
}
