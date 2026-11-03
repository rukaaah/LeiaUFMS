package br.ufms.leiaufms.data.local.model;

import androidx.room.Embedded;

import br.ufms.leiaufms.data.local.entity.LivroCatalogo;

/** Livro do catálogo com o tópico que o liga ao curso e o peso do tópico (quantas disciplinas do curso). */
public class GradeRow {
    @Embedded
    public LivroCatalogo livro;
    public long topicoId;
    public int peso;
}
