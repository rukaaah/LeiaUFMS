package br.ufms.leiaufms.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Nota de 1 a 5 após a troca (RF-25). Única por (pedido, avaliador). */
@Entity(tableName = "avaliacao",
        indices = {@Index(value = {"pedidoId", "avaliadorId"}, unique = true), @Index("avaliadoId")})
public class Avaliacao {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long pedidoId;

    public long avaliadorId;

    public long avaliadoId;

    public int nota;

    public long criadaEm;
}
