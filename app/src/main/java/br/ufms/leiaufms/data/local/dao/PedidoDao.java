package br.ufms.leiaufms.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import br.ufms.leiaufms.data.local.entity.Pedido;
import br.ufms.leiaufms.data.local.model.PedidoRow;

/** Pedidos (RF-18 a RF-26). Mudanças de estado passam pelo TradeRepository, em transação. */
@Dao
public interface PedidoDao {

    String SELECT_ROW = "SELECT pd.*, o.titulo, o.autor, COALESCE(o.capaPath, e.fotoPath) AS capaPath, o.id AS obraId, "
            + "sol.nome AS solicitanteNome, dono.nome AS donoNome, oo.titulo AS oferecidoTitulo, "
            + "pt.nome AS pontoNome, pt.descricao AS pontoDescricao, pt.lat AS pontoLat, pt.lng AS pontoLng "
            + "FROM pedido pd JOIN anuncio a ON a.id = pd.anuncioId JOIN exemplar e ON e.id = a.exemplarId "
            + "JOIN obra o ON o.id = e.obraId JOIN usuario sol ON sol.id = pd.solicitanteId "
            + "JOIN usuario dono ON dono.id = pd.donoId "
            + "LEFT JOIN exemplar eo ON eo.id = pd.exemplarOferecidoId LEFT JOIN obra oo ON oo.id = eo.obraId "
            + "LEFT JOIN ponto_encontro pt ON pt.id = pd.pontoEncontroId ";

    @Insert
    long insert(Pedido pedido);

    @Update
    void update(Pedido pedido);

    @Query("SELECT * FROM pedido WHERE id = :id")
    Pedido findById(long id);

    @Query(SELECT_ROW + "WHERE pd.id = :id")
    LiveData<PedidoRow> observeRow(long id);

    @Query(SELECT_ROW + "WHERE pd.donoId = :usuarioId ORDER BY pd.atualizadoEm DESC")
    LiveData<List<PedidoRow>> received(long usuarioId);

    @Query(SELECT_ROW + "WHERE pd.solicitanteId = :usuarioId ORDER BY pd.atualizadoEm DESC")
    LiveData<List<PedidoRow>> sent(long usuarioId);

    @Query("SELECT * FROM pedido WHERE anuncioId = :anuncioId AND estado = 'PENDENTE' AND id != :exceptId")
    List<Pedido> otherPending(long anuncioId, long exceptId);

    @Query("SELECT * FROM pedido WHERE anuncioId = :anuncioId AND solicitanteId = :usuarioId "
            + "AND estado IN ('PENDENTE', 'ACEITO', 'EM_DISPUTA') LIMIT 1")
    Pedido activeFor(long anuncioId, long usuarioId);

    @Query("SELECT COUNT(*) FROM pedido WHERE solicitanteId = :usuarioId AND estado = 'PENDENTE'")
    int countPendingBy(long usuarioId);

    /** RN-TROCA-03: exemplar já oferecido num pedido aceito ou em disputa. */
    @Query("SELECT COUNT(*) FROM pedido WHERE exemplarOferecidoId = :exemplarId AND estado IN ('ACEITO', 'EM_DISPUTA')")
    int countOfferAccepted(long exemplarId);

    @Query("SELECT * FROM pedido WHERE estado = 'PENDENTE' AND criadoEm < :limite")
    List<Pedido> pendingOlderThan(long limite);

    @Query("SELECT * FROM pedido WHERE estado = 'ACEITO' AND aceitoEm < :limite")
    List<Pedido> acceptedOlderThan(long limite);

    @Query("SELECT * FROM pedido WHERE estado = 'ACEITO' AND lembreteEnviado = 0 AND aceitoEm < :limite")
    List<Pedido> needingDeadlineReminder(long limite);

    @Query("SELECT * FROM pedido WHERE estado = 'ACEITO' AND lembreteEncontroEnviado = 0 "
            + "AND encontroEm IS NOT NULL AND encontroAceito = 1 AND encontroEm BETWEEN :agora AND :ate")
    List<Pedido> meetingsSoon(long agora, long ate);

    @Query("SELECT COUNT(*) FROM pedido WHERE estado = 'CONCLUIDO' AND (solicitanteId = :usuarioId OR donoId = :usuarioId)")
    int countConcluded(long usuarioId);

    @Query("SELECT COUNT(*) FROM pedido WHERE estado = 'CONCLUIDO' AND donoId = :usuarioId AND tipo = 'RESGATE'")
    int countDonations(long usuarioId);

    /** Próximo encontro marcado (até 30 min depois do horário), para a faixa fixa do app. */
    @Query("SELECT pd.id AS pedidoId, o.titulo, o.capaPath, pd.encontroEm, pt.nome AS pontoNome, pt.lat AS pontoLat, "
            + "pt.lng AS pontoLng, CASE WHEN pd.donoId = :me THEN sol.nome ELSE dono.nome END AS outroNome, "
            + "le.lat AS outroLat, le.lng AS outroLng, le.atualizadaEm AS outroAtualizadoEm "
            + "FROM pedido pd JOIN anuncio a ON a.id = pd.anuncioId JOIN exemplar e ON e.id = a.exemplarId "
            + "JOIN obra o ON o.id = e.obraId JOIN usuario sol ON sol.id = pd.solicitanteId "
            + "JOIN usuario dono ON dono.id = pd.donoId LEFT JOIN ponto_encontro pt ON pt.id = pd.pontoEncontroId "
            + "LEFT JOIN localizacao_encontro le ON le.pedidoId = pd.id "
            + "AND le.usuarioId = CASE WHEN pd.donoId = :me THEN pd.solicitanteId ELSE pd.donoId END "
            + "WHERE pd.estado = 'ACEITO' AND pd.encontroEm IS NOT NULL AND pd.encontroAceito = 1 AND (pd.donoId = :me OR pd.solicitanteId = :me) "
            + "AND pd.encontroEm >= :desde ORDER BY pd.encontroEm LIMIT 1")
    LiveData<br.ufms.leiaufms.data.local.model.MeetingRow> nextMeeting(long me, long desde);

    @Query(SELECT_ROW + "WHERE pd.estado = 'EM_DISPUTA' ORDER BY pd.atualizadoEm")
    LiveData<List<PedidoRow>> disputes();
}
