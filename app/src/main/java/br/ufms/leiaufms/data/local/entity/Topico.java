package br.ufms.leiaufms.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Assunto que liga livros a disciplinas (ex.: "Cálculo", "Direito civil"). RF-45. */
@Entity(tableName = "topico")
public class Topico {

    @PrimaryKey
    public long id;

    @NonNull
    public String nome = "";

    public String genero;
}
