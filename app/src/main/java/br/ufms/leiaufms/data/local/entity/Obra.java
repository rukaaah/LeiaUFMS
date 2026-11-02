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
}
