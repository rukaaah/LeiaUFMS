package br.ufms.leiaufms.data.local.dao;

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

    @Query("SELECT AVG(nota) FROM avaliacao WHERE avaliadoId = :usuarioId")
    Double averageRating(long usuarioId);

    @Query("SELECT COUNT(*) FROM avaliacao WHERE avaliadoId = :usuarioId")
    int ratingCount(long usuarioId);
}
