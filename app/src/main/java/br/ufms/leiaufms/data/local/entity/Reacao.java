package br.ufms.leiaufms.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;

/** Coração ou salvo (RF-39). Um de cada tipo por usuário por post; o coração estende a vida do post no mural. */
@Entity(tableName = "reacao", primaryKeys = {"postId", "usuarioId", "tipo"})
public class Reacao {

    public static final String TIPO_CORACAO = "CORACAO";
    public static final String TIPO_SALVO = "SALVO";


    public long postId;

    public long usuarioId;

    @NonNull
    public String tipo = "";
}
