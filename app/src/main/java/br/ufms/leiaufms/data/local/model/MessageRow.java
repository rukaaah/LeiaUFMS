package br.ufms.leiaufms.data.local.model;

/** Mensagem do chat com o nome de quem escreveu. */
public class MessageRow {
    public long id;
    public long autorId;
    public String autorNome;
    public String texto;
    public boolean pronta;
    public long enviadaEm;
}
