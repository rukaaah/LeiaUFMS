package br.ufms.leiaufms.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Comentário em um post do mural (RF-43). Some junto com o post quando ele é ocultado. */
@Entity(tableName = "comentario_post",
        indices = {@Index("postId")})
public class ComentarioPost {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long postId;

    public long autorId;

    @NonNull
    public String texto = "";

    public long criadoEm;
}
