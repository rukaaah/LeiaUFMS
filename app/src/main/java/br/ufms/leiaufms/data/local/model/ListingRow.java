package br.ufms.leiaufms.data.local.model;

/** Anúncio com obra, exemplar, dono e ponto preferido (consulta de Explorar e do detalhe). */
public class ListingRow {
    public long anuncioId;
    public String modalidade;
    public Double lat;
    public Double lng;
    public boolean ativo;
    public long criadoEm;
    public long donoId;
    public Long pontoPreferidoId;
    public long exemplarId;
    public String edicao;
    public String conservacao;
    public String fotoPath;
    public long obraId;
    public String titulo;
    public String autor;
    public String isbn13;
    public String capaPath;
    public String genero;
    public String donoNome;
    public String donoCurso;
    public String donoFoto;
    public String pontoNome;
    public Double pontoLat;
    public Double pontoLng;

    /** Distância calculada na tela (metros), ou null sem localização. */
    public transient Float distancia;

    /** Cópia rasa, para a lista recalcular distâncias sem mexer nos objetos já exibidos. */
    public ListingRow copy() {
        ListingRow c = new ListingRow();
        c.anuncioId = anuncioId;
        c.modalidade = modalidade;
        c.lat = lat;
        c.lng = lng;
        c.ativo = ativo;
        c.criadoEm = criadoEm;
        c.donoId = donoId;
        c.pontoPreferidoId = pontoPreferidoId;
        c.exemplarId = exemplarId;
        c.edicao = edicao;
        c.conservacao = conservacao;
        c.fotoPath = fotoPath;
        c.obraId = obraId;
        c.titulo = titulo;
        c.autor = autor;
        c.isbn13 = isbn13;
        c.capaPath = capaPath;
        c.genero = genero;
        c.donoNome = donoNome;
        c.donoCurso = donoCurso;
        c.donoFoto = donoFoto;
        c.pontoNome = pontoNome;
        c.pontoLat = pontoLat;
        c.pontoLng = pontoLng;
        return c;
    }

    /** Imagem do card: capa da obra; sem capa, a foto do exemplar. */
    public String imagem() {
        return capaPath != null ? capaPath : fotoPath;
    }

    public Double latitude() {
        return lat != null ? lat : pontoLat;
    }

    public Double longitude() {
        return lng != null ? lng : pontoLng;
    }
}
