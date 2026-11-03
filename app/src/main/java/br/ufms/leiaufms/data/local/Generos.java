package br.ufms.leiaufms.data.local;

/** Gêneros usados para personalizar o Início e filtrar "Todos os livros" (feedback 03/11). */
public final class Generos {

    public static final String LIT_BRASILEIRA = "Literatura brasileira";
    public static final String LIT_ESTRANGEIRA = "Literatura estrangeira";
    public static final String EXATAS = "Exatas";
    public static final String COMPUTACAO = "Computação";
    public static final String HUMANAS = "Ciências humanas";
    public static final String SAUDE = "Saúde";
    public static final String OUTROS = "Outros";

    public static final String[] TODOS = {LIT_BRASILEIRA, LIT_ESTRANGEIRA, EXATAS, COMPUTACAO, HUMANAS, SAUDE, OUTROS};

    private Generos() {
    }
}
