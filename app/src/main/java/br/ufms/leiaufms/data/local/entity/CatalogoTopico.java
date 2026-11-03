package br.ufms.leiaufms.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;

/** Livro do catálogo ↔ tópico. */
@Entity(tableName = "catalogo_topico", primaryKeys = {"livroId", "topicoId"}, indices = {@Index("topicoId")})
public class CatalogoTopico {
    public long livroId;
    public long topicoId;
}
