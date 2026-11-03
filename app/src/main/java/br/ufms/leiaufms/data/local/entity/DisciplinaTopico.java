package br.ufms.leiaufms.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;

/** Disciplina da UFMS ↔ tópico (pelo nome da disciplina, tools/catalogo/topicos.py). */
@Entity(tableName = "disciplina_topico", primaryKeys = {"disciplinaId", "topicoId"}, indices = {@Index("topicoId")})
public class DisciplinaTopico {
    public long disciplinaId;
    public long topicoId;
}
