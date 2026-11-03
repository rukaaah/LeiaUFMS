package br.ufms.leiaufms.data.local.model;

/** Post do mural com a obra, o autor e as reações (RF-38, RF-39). */
public class PostRow {
    public long id;
    public long autorId;
    public String autorNome;
    public long obraId;
    public String titulo;
    public String autor;
    public String capaPath;
    public String tipo;
    public String texto;
    public Long anuncioId;
    public long criadoEm;
    public int uteis;
    public int queros;
    public boolean meuUtil;
    public boolean meuQuero;
    public int comentarios;
}
