package br.ufms.leiaufms.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;

/** Obra do app ↔ tópico (copiado do catálogo quando a obra é criada a partir dele). */
@Entity(tableName = "obra_topico", primaryKeys = {"obraId", "topicoId"}, indices = {@Index("topicoId")})
public class ObraTopico {
    public long obraId;
    public long topicoId;
}
