package br.ufms.leiaufms.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Disciplina com obras recomendadas (RF-16). */
@Entity(tableName = "disciplina", indices = {@androidx.room.Index("curso")})
public class Disciplina {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String codigo;

    @NonNull
    public String nome = "";

    public String curso;
}
