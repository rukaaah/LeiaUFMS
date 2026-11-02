package br.ufms.leiaufms.data.local.model;

/** Item da estante ou da lista de desejos, com a obra e quantos anúncios ativos de outras pessoas existem. */
public class ShelfRow {
    public long id;
    public long obraId;
    public String status;
    public String titulo;
    public String autor;
    public int disponiveis;
    public Long anuncioId;
}
