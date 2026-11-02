package br.ufms.leiaufms.data.local.entity;

import androidx.room.Entity;

/** Edições compatíveis marcadas pelo Moderador. */
@Entity(tableName = "edicao_compativel", primaryKeys = {"obraId", "obraCompativelId"})
public class EdicaoCompativel {

    public long obraId;

    public long obraCompativelId;
}
