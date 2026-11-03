package br.ufms.leiaufms.data.local.model;

/** Próximo encontro do usuário, com o ponto e a última posição compartilhada pela outra parte (RF-41). */
public class MeetingRow {
    public long pedidoId;
    public String titulo;
    public String capaPath;
    public long encontroEm;
    public String pontoNome;
    public Double pontoLat;
    public Double pontoLng;
    public String outroNome;
    public Double outroLat;
    public Double outroLng;
    public Long outroAtualizadoEm;
}
