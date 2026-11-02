package br.ufms.leiaufms.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Objeto físico de uma obra. */
@Entity(tableName = "exemplar",
        indices = {@Index("obraId"), @Index("donoAtualId")})
public class Exemplar {

    public static final String CONSERVACAO_NOVO = "NOVO";
    public static final String CONSERVACAO_BOM = "BOM";
    public static final String CONSERVACAO_USADO = "USADO";
    public static final String CONSERVACAO_GASTO = "GASTO";


    @PrimaryKey(autoGenerate = true)
    public long id;

    public long obraId;

    public long donoAtualId;

    public String edicao;

    @NonNull
    public String conservacao = "";

    public String fotoPath;
}
