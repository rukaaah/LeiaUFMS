package br.ufms.leiaufms.data.local.entity;

import androidx.room.Entity;

/**
 * RF-41: última posição que uma parte compartilhou para o encontro. Só existe enquanto a pessoa
 * mantém o compartilhamento ligado e dentro da janela (10 min antes até 30 min depois).
 */
@Entity(tableName = "localizacao_encontro", primaryKeys = {"pedidoId", "usuarioId"})
public class LocalizacaoEncontro {

    public long pedidoId;

    public long usuarioId;

    public double lat;

    public double lng;

    public long atualizadaEm;
}
