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

    // ---- Mural (RF-38, RF-39): finito; cada post vive 12 h + 12 h a cada 25 corações (PostLifetime).
    // "Salvos" (combinável com o tipo): os posts que a pessoa salvou, sem prazo.
    @Query("SELECT p.id, p.autorId, u.nome AS autorNome, p.obraId, o.titulo, o.autor, o.capaPath, p.tipo, p.texto, "
            + "p.anuncioId, p.criadoEm, "
            + "(SELECT COUNT(*) FROM reacao r WHERE r.postId = p.id AND r.tipo = 'CORACAO') AS coracoes, "
            + "EXISTS (SELECT 1 FROM reacao r WHERE r.postId = p.id AND r.tipo = 'CORACAO' AND r.usuarioId = :me) AS meuCoracao, "
            + "EXISTS (SELECT 1 FROM reacao r WHERE r.postId = p.id AND r.tipo = 'SALVO' AND r.usuarioId = :me) AS meuSalvo, "
            + "(SELECT COUNT(*) FROM comentario_post c WHERE c.postId = p.id) AS comentarios "
            + "FROM post_mural p JOIN obra o ON o.id = p.obraId JOIN usuario u ON u.id = p.autorId "
            + "WHERE p.oculto = 0 AND (:tipo = '' OR p.tipo = :tipo) AND ("
            + " (:salvos = 1 AND EXISTS (SELECT 1 FROM reacao r WHERE r.postId = p.id AND r.tipo = 'SALVO' AND r.usuarioId = :me)) "
            + " OR (:salvos = 0 "
            + "  AND p.criadoEm + (12 + 12 * ((SELECT COUNT(*) FROM reacao r WHERE r.postId = p.id AND r.tipo = 'CORACAO') / 25)) * 3600000 >= :agora)) "
            + "ORDER BY p.criadoEm DESC LIMIT :limite")
    LiveData<List<PostRow>> feed(long me, long agora, String tipo, boolean salvos, int limite);

    @Query("SELECT p.id, p.autorId, u.nome AS autorNome, p.obraId, o.titulo, o.autor, o.capaPath, p.tipo, p.texto, "
            + "p.anuncioId, p.criadoEm, "
            + "(SELECT COUNT(*) FROM reacao r WHERE r.postId = p.id AND r.tipo = 'CORACAO') AS coracoes, "
            + "EXISTS (SELECT 1 FROM reacao r WHERE r.postId = p.id AND r.tipo = 'CORACAO' AND r.usuarioId = :me) AS meuCoracao, "
            + "EXISTS (SELECT 1 FROM reacao r WHERE r.postId = p.id AND r.tipo = 'SALVO' AND r.usuarioId = :me) AS meuSalvo, "
            + "(SELECT COUNT(*) FROM comentario_post c WHERE c.postId = p.id) AS comentarios "
            + "FROM post_mural p JOIN obra o ON o.id = p.obraId JOIN usuario u ON u.id = p.autorId "
            + "WHERE p.id = :postId AND p.oculto = 0")
    LiveData<PostRow> post(long me, long postId);

    // ---- Comentários do mural (RF-43)
    @Query("SELECT c.id, c.autorId, u.nome AS autorNome, c.texto, 0 AS pronta, c.criadoEm AS enviadaEm "
            + "FROM comentario_post c JOIN usuario u ON u.id = c.autorId WHERE c.postId = :postId ORDER BY c.criadoEm, c.id")
    LiveData<List<MessageRow>> comments(long postId);

    @Insert
    long insertComentario(br.ufms.leiaufms.data.local.entity.ComentarioPost c);

    @Query("SELECT * FROM post_mural WHERE id = :id")
    br.ufms.leiaufms.data.local.entity.PostMural postById(long id);

    @Query("DELETE FROM reacao WHERE postId = :postId AND usuarioId = :usuarioId AND tipo = :tipo")
    int deleteReacao(long postId, long usuarioId, String tipo);

    @Query("UPDATE post_mural SET oculto = 1 WHERE id = :postId")
    void hidePost(long postId);

    // ---- Chat (RF-40)
    @Query("SELECT m.id, m.autorId, u.nome AS autorNome, m.texto, m.pronta, m.enviadaEm FROM mensagem m "
            + "JOIN usuario u ON u.id = m.autorId WHERE m.pedidoId = :pedidoId ORDER BY m.enviadaEm, m.id")
    LiveData<List<MessageRow>> messages(long pedidoId);

    // ---- Estante (RF-35)
    @Query("SELECT i.id, i.obraId, i.status, o.titulo, o.autor, o.capaPath, 0 AS disponiveis, NULL AS anuncioId, 0 AS outrosQuerem "
            + "FROM item_estante i JOIN obra o ON o.id = i.obraId WHERE i.usuarioId = :usuarioId "
            + "ORDER BY CASE i.status WHEN 'LENDO' THEN 0 WHEN 'LIDO' THEN 1 ELSE 2 END, i.atualizadoEm DESC")
    LiveData<List<ShelfRow>> shelf(long usuarioId);

    /** Último livro lido ou em leitura (base do "Porque você leu"). */
    @Query("SELECT i.id, i.obraId, i.status, o.titulo, o.autor, o.capaPath, 0 AS disponiveis, NULL AS anuncioId, 0 AS outrosQuerem "
            + "FROM item_estante i JOIN obra o ON o.id = i.obraId WHERE i.usuarioId = :usuarioId "
            + "AND i.status IN ('LIDO', 'LENDO', 'PASSADO_ADIANTE') ORDER BY i.atualizadoEm DESC LIMIT 1")
    LiveData<ShelfRow> lastRead(long usuarioId);

    @Query("SELECT * FROM item_estante WHERE id = :id")
    ItemEstante shelfItem(long id);

    @Query("DELETE FROM item_estante WHERE id = :id AND usuarioId = :usuarioId")
    void deleteShelf(long id, long usuarioId);

    // ---- Desejos (RF-37)
    @Query("SELECT d.id, d.obraId, 'DESEJO' AS status, o.titulo, o.autor, o.capaPath, "
            + "(SELECT COUNT(*) FROM anuncio a JOIN exemplar e ON e.id = a.exemplarId "
            + " WHERE e.obraId = d.obraId AND a.ativo = 1 AND a.donoId != d.usuarioId) AS disponiveis, "
            + "(SELECT a.id FROM anuncio a JOIN exemplar e ON e.id = a.exemplarId "
            + " WHERE e.obraId = d.obraId AND a.ativo = 1 AND a.donoId != d.usuarioId ORDER BY a.criadoEm DESC LIMIT 1) AS anuncioId, "
            + "(SELECT COUNT(*) FROM desejo d2 WHERE d2.obraId = d.obraId AND d2.usuarioId != d.usuarioId) AS outrosQuerem "
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
