package br.ufms.leiaufms.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Agrupador do hub da obra (RF-15). */
@Entity(tableName = "obra",
        indices = {@Index("isbn13"), @Index({"titulo", "autor"})})
public class Obra {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String titulo = "";

    @NonNull
    public String autor = "";

    public String isbn13;

    /** Gênero da obra (lista fixa em Generos), usado nas categorias do Início. */
    public String genero;

    /** Capa baixada da Open Library e guardada em filesDir/capas (null enquanto não houver). */
    public String capaPath;

    /** Última tentativa de buscar a capa (evita repetir a busca a cada abertura). */
    public Long capaTentativaEm;
}
