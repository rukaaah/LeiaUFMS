package br.ufms.leiaufms.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;

/** Reação "útil" ou "quero" (RF-39). Uma de cada tipo por usuário por post. */
@Entity(tableName = "reacao", primaryKeys = {"postId", "usuarioId", "tipo"})
public class Reacao {

    public static final String TIPO_UTIL = "UTIL";
    public static final String TIPO_QUERO = "QUERO";


    public long postId;

    public long usuarioId;

    @NonNull
    public String tipo = "";
}
