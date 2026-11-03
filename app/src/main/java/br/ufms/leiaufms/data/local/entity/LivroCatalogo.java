package br.ufms.leiaufms.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Livro do catálogo de referência (Open Library, CC0), carregado de assets/catalogo.json.
 * Serve para padronizar título e autor e para recomendar por disciplina (RF-45).
 */
@Entity(tableName = "livro_catalogo", indices = {@Index(value = "chave", unique = true), @Index("titulo")})
public class LivroCatalogo {

    @PrimaryKey
    public long id;

    @NonNull
    public String titulo = "";

    @NonNull
    public String autor = "";

    /** Título e autor sem acento e em minúsculas ("titulo|autor"), para achar o livro mesmo digitado diferente. */
    @NonNull
    public String chave = "";

    public String isbn13;

    public Integer ano;

    /** Id da capa na Open Library (covers.openlibrary.org/b/id/{id}-M.jpg). */
    public Long capaOl;

    public String genero;
}
