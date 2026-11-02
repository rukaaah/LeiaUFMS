package br.ufms.leiaufms.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Sessão ativa: só o hash SHA-256 do token fica no banco (ADR-04). */
@Entity(tableName = "sessao",
        indices = {@Index(value = "tokenHash", unique = true), @Index("usuarioId")})
public class Sessao {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long usuarioId;

    @NonNull
    public String tokenHash = "";

    public long criadaEm;

    public long expiraEm;
}
