package br.ufms.leiaufms.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Post do mural; obra obrigatória (RF-38). */
@Entity(tableName = "post_mural",
        indices = {@Index("criadoEm"), @Index("obraId")})
public class PostMural {

    public static final String TIPO_DICA = "DICA";
    public static final String TIPO_PROCURO = "PROCURO";
    public static final String TIPO_DOACAO = "DOACAO";
    public static final String TIPO_TROCA_CONCLUIDA = "TROCA_CONCLUIDA";


    @PrimaryKey(autoGenerate = true)
    public long id;

    public long autorId;

    public long obraId;

    @NonNull
    public String tipo = "";

    @NonNull
    public String texto = "";

    public Long anuncioId;

    public boolean oculto;

    public long criadoEm;
}
