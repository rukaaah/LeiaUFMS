package br.ufms.leiaufms.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import br.ufms.leiaufms.data.local.entity.Avaliacao;
import br.ufms.leiaufms.data.local.entity.Desejo;
import br.ufms.leiaufms.data.local.entity.ItemEstante;
import br.ufms.leiaufms.data.local.entity.Mensagem;
import br.ufms.leiaufms.data.local.entity.PostMural;
import br.ufms.leiaufms.data.local.entity.Reacao;
import br.ufms.leiaufms.data.local.model.MessageRow;
import br.ufms.leiaufms.data.local.model.PostRow;
import br.ufms.leiaufms.data.local.model.ShelfRow;

import java.util.List;

/** Estante, desejos, mural, reações, chat e avaliações (RF-25, RF-35 a RF-40). */
@Dao
public interface SocialDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long upsertEstante(ItemEstante item);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertDesejo(Desejo desejo);

    @Insert
    long insertPost(PostMural post);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertReacao(Reacao reacao);

    @Insert
    long insertMensagem(Mensagem mensagem);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long upsertAvaliacao(Avaliacao avaliacao);

    // ---- Mural (RF-38, RF-39): finito, últimos 14 dias, até 30 itens
    @Query("SELECT p.id, p.autorId, u.nome AS autorNome, p.obraId, o.titulo, o.autor, o.capaPath, p.tipo, p.texto, "
            + "p.anuncioId, p.criadoEm, "
            + "(SELECT COUNT(*) FROM reacao r WHERE r.postId = p.id AND r.tipo = 'UTIL') AS uteis, "
            + "(SELECT COUNT(*) FROM reacao r WHERE r.postId = p.id AND r.tipo = 'QUERO') AS queros, "
            + "EXISTS (SELECT 1 FROM reacao r WHERE r.postId = p.id AND r.tipo = 'UTIL' AND r.usuarioId = :me) AS meuUtil, "
            + "EXISTS (SELECT 1 FROM reacao r WHERE r.postId = p.id AND r.tipo = 'QUERO' AND r.usuarioId = :me) AS meuQuero "
            + "FROM post_mural p JOIN obra o ON o.id = p.obraId JOIN usuario u ON u.id = p.autorId "
            + "WHERE p.oculto = 0 AND p.criadoEm >= :desde AND (:tipo = '' OR p.tipo = :tipo) "
            + "ORDER BY p.criadoEm DESC LIMIT :limite")
    LiveData<List<PostRow>> feed(long me, long desde, String tipo, int limite);

    @Query("DELETE FROM reacao WHERE postId = :postId AND usuarioId = :usuarioId AND tipo = :tipo")
    int deleteReacao(long postId, long usuarioId, String tipo);

    @Query("UPDATE post_mural SET oculto = 1 WHERE id = :postId")
    void hidePost(long postId);

    // ---- Chat (RF-40)
    @Query("SELECT m.id, m.autorId, u.nome AS autorNome, m.texto, m.pronta, m.enviadaEm FROM mensagem m "
            + "JOIN usuario u ON u.id = m.autorId WHERE m.pedidoId = :pedidoId ORDER BY m.enviadaEm, m.id")
    LiveData<List<MessageRow>> messages(long pedidoId);

    // ---- Estante (RF-35)
    @Query("SELECT i.id, i.obraId, i.status, o.titulo, o.autor, o.capaPath, 0 AS disponiveis, NULL AS anuncioId "
            + "FROM item_estante i JOIN obra o ON o.id = i.obraId WHERE i.usuarioId = :usuarioId "
            + "ORDER BY CASE i.status WHEN 'LENDO' THEN 0 WHEN 'LIDO' THEN 1 ELSE 2 END, i.atualizadoEm DESC")
    LiveData<List<ShelfRow>> shelf(long usuarioId);

    @Query("SELECT * FROM item_estante WHERE id = :id")
    ItemEstante shelfItem(long id);

    @Query("DELETE FROM item_estante WHERE id = :id AND usuarioId = :usuarioId")
    void deleteShelf(long id, long usuarioId);

    // ---- Desejos (RF-37)
    @Query("SELECT d.id, d.obraId, 'DESEJO' AS status, o.titulo, o.autor, o.capaPath, "
            + "(SELECT COUNT(*) FROM anuncio a JOIN exemplar e ON e.id = a.exemplarId "
            + " WHERE e.obraId = d.obraId AND a.ativo = 1 AND a.donoId != d.usuarioId) AS disponiveis, "
            + "(SELECT a.id FROM anuncio a JOIN exemplar e ON e.id = a.exemplarId "
            + " WHERE e.obraId = d.obraId AND a.ativo = 1 AND a.donoId != d.usuarioId ORDER BY a.criadoEm DESC LIMIT 1) AS anuncioId "
            + "FROM desejo d JOIN obra o ON o.id = d.obraId WHERE d.usuarioId = :usuarioId ORDER BY d.criadoEm DESC")
    LiveData<List<ShelfRow>> wishes(long usuarioId);

    @Query("DELETE FROM desejo WHERE id = :id AND usuarioId = :usuarioId")
    void deleteWish(long id, long usuarioId);

    @Query("SELECT obraId FROM desejo WHERE usuarioId = :usuarioId")
    LiveData<List<Long>> wishedObras(long usuarioId);

    @Query("DELETE FROM desejo WHERE usuarioId = :usuarioId AND obraId = :obraId")
    void deleteWishByObra(long usuarioId, long obraId);

    /** Prova social leve: quantas pessoas querem a obra. */
    @Query("SELECT COUNT(*) FROM desejo WHERE obraId = :obraId")
    int wishCount(long obraId);

    @Query("SELECT COUNT(*) FROM desejo WHERE usuarioId = :usuarioId AND obraId = :obraId")
    int hasWish(long usuarioId, long obraId);

    // ---- Avaliações (RF-25)
    @Query("SELECT * FROM avaliacao WHERE pedidoId = :pedidoId AND avaliadorId = :avaliadorId LIMIT 1")
    Avaliacao rating(long pedidoId, long avaliadorId);

    @Query("SELECT usuarioId FROM desejo WHERE obraId = :obraId AND usuarioId != :exceptUserId")
    java.util.List<Long> wishersOf(long obraId, long exceptUserId);

    @Query("SELECT AVG(nota) FROM avaliacao WHERE avaliadoId = :usuarioId")
    Double averageRating(long usuarioId);

    @Query("SELECT COUNT(*) FROM avaliacao WHERE avaliadoId = :usuarioId")
    int ratingCount(long usuarioId);
}
