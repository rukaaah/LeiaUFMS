package br.ufms.leiaufms.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Lista de desejos (RF-37). */
@Entity(tableName = "desejo",
        indices = {@Index(value = {"usuarioId", "obraId"}, unique = true), @Index("obraId")})
public class Desejo {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long usuarioId;

    public long obraId;

    public long criadoEm;
}
