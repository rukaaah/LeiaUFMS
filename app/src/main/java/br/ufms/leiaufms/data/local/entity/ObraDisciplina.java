package br.ufms.leiaufms.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;

/** Obra recomendada para uma disciplina. */
@Entity(tableName = "obra_disciplina", primaryKeys = {"obraId", "disciplinaId"},
        indices = {@Index("disciplinaId")})
public class ObraDisciplina {

    public long obraId;

    public long disciplinaId;
}
