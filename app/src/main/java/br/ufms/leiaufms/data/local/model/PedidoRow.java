package br.ufms.leiaufms.data.local.model;

import androidx.room.Embedded;

import br.ufms.leiaufms.data.local.entity.Pedido;

/** Pedido com o livro, as partes e o ponto de encontro (aba Trocas e detalhe do pedido). */
public class PedidoRow {
    @Embedded
    public Pedido pedido;
    public String titulo;
    public String autor;
    public String capaPath;
    public long obraId;
    public String solicitanteNome;
    public String donoNome;
    public String oferecidoTitulo;
    public String pontoNome;
    public String pontoDescricao;
}
