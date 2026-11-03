package br.ufms.leiaufms.data.local;

/**
 * Gêneros usados para personalizar o Início e filtrar "Todos os livros".
 * Ampliados em 03/11 para cobrir as áreas dos cursos da UFMS (mesma lista de tools/catalogo/topicos.py).
 */
public final class Generos {

    public static final String LIT_BRASILEIRA = "Literatura brasileira";
    public static final String LIT_ESTRANGEIRA = "Literatura estrangeira";
    public static final String EXATAS = "Exatas";
    public static final String COMPUTACAO = "Computação";
    public static final String ENGENHARIAS = "Engenharias";
    public static final String SAUDE = "Saúde";
    public static final String BIO_AGRARIAS = "Biológicas e agrárias";
    public static final String HUMANAS = "Ciências humanas";
    public static final String EDUCACAO = "Educação";
    public static final String DIREITO = "Direito";
    public static final String NEGOCIOS = "Negócios";
    public static final String ARTES = "Artes e comunicação";
    public static final String OUTROS = "Outros";

    public static final String[] TODOS = {LIT_BRASILEIRA, LIT_ESTRANGEIRA, EXATAS, COMPUTACAO, ENGENHARIAS, SAUDE,
            BIO_AGRARIAS, HUMANAS, EDUCACAO, DIREITO, NEGOCIOS, ARTES, OUTROS};

    private Generos() {
    }
}
