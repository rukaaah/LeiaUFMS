package br.ufms.leiaufms.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Aluno ou Moderador. Nunca guarda senha, só o hash BCrypt (RN-AUTH-05). */
@Entity(tableName = "usuario",
        indices = {@Index(value = "email", unique = true)})
public class Usuario {

    public static final String PAPEL_ALUNO = "ALUNO";
    public static final String PAPEL_MODERADOR = "MODERADOR";

    public boolean isModerador() {
        return PAPEL_MODERADOR.equals(papel);
    }


    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String nome = "";

    @NonNull
    public String email = "";

    @NonNull
    public String senhaHash = "";

    @NonNull
    public String curso = "";

    public int semestre;

    public String status;

    public String fotoPath;

    @NonNull
    public String papel = "";

    public long criadoEm;
}
