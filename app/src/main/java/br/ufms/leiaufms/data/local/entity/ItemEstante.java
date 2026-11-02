package br.ufms.leiaufms.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Estante pública (RF-35). */
@Entity(tableName = "item_estante",
        indices = {@Index(value = {"usuarioId", "obraId"}, unique = true)})
public class ItemEstante {

    public static final String STATUS_LENDO = "LENDO";
    public static final String STATUS_LIDO = "LIDO";
    public static final String STATUS_PASSADO_ADIANTE = "PASSADO_ADIANTE";


    @PrimaryKey(autoGenerate = true)
    public long id;

    public long usuarioId;

    public long obraId;

    @NonNull
    public String status = "";

    public long atualizadoEm;
}
