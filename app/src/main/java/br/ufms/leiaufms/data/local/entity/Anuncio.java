package br.ufms.leiaufms.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Anúncio de um exemplar. Coordenadas vêm da localização escolhida no app (nunca do seed). */
@Entity(tableName = "anuncio",
        indices = {@Index("exemplarId"), @Index("donoId"), @Index("ativo")})
public class Anuncio {

    public static final String MODALIDADE_TROCA = "TROCA";
    public static final String MODALIDADE_DOACAO = "DOACAO";
    public static final String MODALIDADE_AMBAS = "AMBAS";

    public boolean aceitaTroca() {
        return !MODALIDADE_DOACAO.equals(modalidade);
    }

    public boolean aceitaDoacao() {
        return !MODALIDADE_TROCA.equals(modalidade);
    }


    @PrimaryKey(autoGenerate = true)
    public long id;

    public long exemplarId;

    public long donoId;

    @NonNull
    public String modalidade = "";

    public Double lat;

    public Double lng;

    public Long pontoPreferidoId;

    public boolean ativo;

    public long criadoEm;
}
