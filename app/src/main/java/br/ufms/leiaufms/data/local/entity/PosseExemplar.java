package br.ufms.leiaufms.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Trajetória do exemplar (RF-17). */
@Entity(tableName = "posse_exemplar",
        indices = {@Index("exemplarId")})
public class PosseExemplar {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long exemplarId;

    public long usuarioId;

    public long desde;

    public Long ate;

    public Long pedidoId;
}
