package br.ufms.leiaufms.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Ponto seguro cadastrado pelo Moderador tocando no mapa (RF-30). Nenhum vem no seed. */
@Entity(tableName = "ponto_encontro")
public class PontoEncontro {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String nome = "";

    public String descricao;

    public double lat;

    public double lng;

    public boolean ativo;

    public long criadoPor;

    public long criadoEm;
}
