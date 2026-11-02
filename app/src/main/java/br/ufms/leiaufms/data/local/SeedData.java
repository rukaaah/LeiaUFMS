package br.ufms.leiaufms.data.local;

import br.ufms.leiaufms.core.security.PasswordHasher;
import br.ufms.leiaufms.data.local.entity.Anuncio;
import br.ufms.leiaufms.data.local.entity.Desejo;
import br.ufms.leiaufms.data.local.entity.Disciplina;
import br.ufms.leiaufms.data.local.entity.Exemplar;
import br.ufms.leiaufms.data.local.entity.ItemEstante;
import br.ufms.leiaufms.data.local.entity.Mensagem;
import br.ufms.leiaufms.data.local.entity.MovimentoCredito;
import br.ufms.leiaufms.data.local.entity.Notificacao;
import br.ufms.leiaufms.data.local.entity.Obra;
import br.ufms.leiaufms.data.local.entity.ObraDisciplina;
import br.ufms.leiaufms.data.local.entity.Pedido;
import br.ufms.leiaufms.data.local.entity.PosseExemplar;
import br.ufms.leiaufms.data.local.entity.PostMural;
import br.ufms.leiaufms.data.local.entity.Reacao;
import br.ufms.leiaufms.data.local.entity.Usuario;
import br.ufms.leiaufms.data.pedido.PedidoEstado;

/**
 * Dados de demonstração (docs/04-arquitetura.md §7). Roda uma vez, na criação do banco.
 * Sem coordenadas inventadas: anúncios do seed não têm local e nenhum ponto seguro é criado
 * (o Moderador cadastra os pontos tocando no mapa, RF-30). Senha das contas: ver README.
 */
public final class SeedData {

    /** Senha de demonstração, documentada no README. Só existe aqui para gerar o hash. */
    private static final String DEMO_PASSWORD = "Leia2026";
    private static final long HOUR = 60L * 60 * 1000;
    private static final long DAY = 24 * HOUR;

    private SeedData() {
    }

    public static void populate(AppDatabase db) {
        if (db.usuarioDao().count() > 0) return;
        db.runInTransaction(() -> new Builder(db).run());
    }

    private static final class Builder {
        private final AppDatabase db;
        private final long now = System.currentTimeMillis();

        Builder(AppDatabase db) {
            this.db = db;
        }

        void run() {
            usuario("Moderação LeiaUFMS", "moderacao@ufms.br", "Coordenação do projeto", 1,
                    Usuario.PAPEL_MODERADOR, "Cuida dos pontos seguros e das disputas");
            long ana = usuario("Ana Souza", "ana.souza@ufms.br", "Letras", 3, Usuario.PAPEL_ALUNO,
                    "Lendo clássicos brasileiros");
            long bruno = usuario("Bruno Lima", "bruno.lima@ufms.br", "Engenharia Civil", 2,
                    Usuario.PAPEL_ALUNO, "Procurando livros de cálculo");
            long carla = usuario("Carla Mendes", "carla.mendes@ufms.br", "Ciência da Computação", 5,
                    Usuario.PAPEL_ALUNO, null);
            long diego = usuario("Diego Rocha", "diego.rocha@ufms.br", "Física", 4,
                    Usuario.PAPEL_ALUNO, "Troco livros de exatas");

            long domCasmurro = obra("Dom Casmurro", "Machado de Assis");
            long bras = obra("Memórias Póstumas de Brás Cubas", "Machado de Assis");
            long vidasSecas = obra("Vidas Secas", "Graciliano Ramos");
            long sertao = obra("Grande Sertão: Veredas", "João Guimarães Rosa");
            long horaEstrela = obra("A Hora da Estrela", "Clarice Lispector");
            long capitaes = obra("Capitães da Areia", "Jorge Amado");
            long calculo = obra("Cálculo, Volume 1", "James Stewart");
            long fisica = obra("Fundamentos de Física, Volume 1", "David Halliday, Robert Resnick, Jearl Walker");
            long algoritmos = obra("Algoritmos: Teoria e Prática", "Thomas H. Cormen e outros");
            long codigoLimpo = obra("Código Limpo", "Robert C. Martin");
            long algebra = obra("Álgebra Linear com Aplicações", "Howard Anton, Chris Rorres");
            long quimica = obra("Química Geral, Volume 1", "John B. Russell");
            long sapiens = obra("Sapiens: Uma Breve História da Humanidade", "Yuval Noah Harari");
            long principe = obra("O Pequeno Príncipe", "Antoine de Saint-Exupéry");
            long orwell = obra("1984", "George Orwell");

            long dCalculo = disciplina("Cálculo Diferencial e Integral I", "Engenharias e Exatas");
            long dAlgo = disciplina("Algoritmos e Programação", "Ciência da Computação");
            long dLit = disciplina("Literatura Brasileira", "Letras");
            recomenda(calculo, dCalculo);
            recomenda(algebra, dCalculo);
            recomenda(algoritmos, dAlgo);
            recomenda(codigoLimpo, dAlgo);
            recomenda(domCasmurro, dLit);
            recomenda(vidasSecas, dLit);
            recomenda(sertao, dLit);
            recomenda(horaEstrela, dLit);

            // Anúncios ativos (sem local: a localização é escolhida no app)
            long aVidasSecas = anuncio(exemplar(vidasSecas, ana, "2ª ed.", Exemplar.CONSERVACAO_BOM),
                    ana, Anuncio.MODALIDADE_DOACAO, 2 * DAY);
            long aDom = anuncio(exemplar(domCasmurro, ana, null, Exemplar.CONSERVACAO_USADO),
                    ana, Anuncio.MODALIDADE_AMBAS, 3 * DAY);
            anuncio(exemplar(bras, ana, null, Exemplar.CONSERVACAO_BOM), ana, Anuncio.MODALIDADE_TROCA, 5 * DAY);
            long exCalculoBruno = exemplar(calculo, bruno, "7ª ed.", Exemplar.CONSERVACAO_GASTO);
            anuncio(exCalculoBruno, bruno, Anuncio.MODALIDADE_TROCA, DAY);
            anuncio(exemplar(sapiens, bruno, null, Exemplar.CONSERVACAO_NOVO), bruno, Anuncio.MODALIDADE_AMBAS, 4 * DAY);
            long aPrincipe = anuncio(exemplar(principe, carla, null, Exemplar.CONSERVACAO_BOM),
                    carla, Anuncio.MODALIDADE_DOACAO, 6 * DAY);
            long aAlgoritmos = anuncio(exemplar(algoritmos, carla, "3ª ed.", Exemplar.CONSERVACAO_BOM),
                    carla, Anuncio.MODALIDADE_TROCA, 8 * DAY);
            long exFisica = exemplar(fisica, diego, "10ª ed.", Exemplar.CONSERVACAO_USADO);
            long aFisica = anuncio(exFisica, diego, Anuncio.MODALIDADE_TROCA, 2 * DAY);
            long exSertao = exemplar(sertao, diego, null, Exemplar.CONSERVACAO_BOM);
            long aSertao = anuncio(exSertao, diego, Anuncio.MODALIDADE_DOACAO, 9 * DAY);
            anuncio(exemplar(orwell, diego, null, Exemplar.CONSERVACAO_USADO), diego, Anuncio.MODALIDADE_AMBAS, 3 * HOUR);
            anuncio(exemplar(algebra, diego, "10ª ed.", Exemplar.CONSERVACAO_BOM), diego, Anuncio.MODALIDADE_DOACAO, 7 * HOUR);
            long exCodigo = exemplar(codigoLimpo, carla, null, Exemplar.CONSERVACAO_NOVO);
            long ex1984Bruno = exemplar(orwell, bruno, null, Exemplar.CONSERVACAO_GASTO);

            // Histórico concluído: gera os créditos do livro-razão de forma consistente
            long exCapitaes = exemplar(capitaes, ana, null, Exemplar.CONSERVACAO_BOM);
            concluirResgate(exCapitaes, anuncioInativo(exCapitaes, ana, 30 * DAY), ana, bruno, false, 28 * DAY);
            long exQuimica = exemplar(quimica, carla, null, Exemplar.CONSERVACAO_USADO);
            concluirResgate(exQuimica, anuncioInativo(exQuimica, carla, 25 * DAY), carla, diego, false, 22 * DAY);
            long exHora = exemplar(horaEstrela, ana, null, Exemplar.CONSERVACAO_BOM);
            concluirResgate(exHora, anuncioInativo(exHora, ana, 15 * DAY), ana, carla, true, 12 * DAY);
            // Saldos depois do histórico: Ana 2, Bruno 0, Carla 0, Diego 0

            // Um pedido em cada estado (aba Trocas)
            long pendente = pedido(aVidasSecas, bruno, ana, Pedido.TIPO_RESGATE, null, PedidoEstado.PENDENTE, true, 5 * HOUR);
            aviso(ana, "Novo pedido de Vidas Secas",
                    "Bruno Lima quer resgatar seu livro. Ele está sem créditos; você decide se aceita.", pendente, 5 * HOUR);
            long aceito = pedido(aFisica, carla, diego, Pedido.TIPO_TROCA_DIRETA, exCodigo, PedidoEstado.ACEITO, false, 2 * DAY);
            aviso(carla, "Pedido aceito", "Diego Rocha aceitou trocar Fundamentos de Física. Combine o encontro.",
                    aceito, 2 * DAY - HOUR);
            mensagem(aceito, diego, "Posso no RU às 12h?", true, DAY);
            mensagem(aceito, carla, "Combinado, levo o Código Limpo.", false, DAY - HOUR);
            pedido(aDom, diego, ana, Pedido.TIPO_TROCA_DIRETA, null, PedidoEstado.RECUSADO, false, 6 * DAY);
            long cancelado = pedido(aPrincipe, ana, carla, Pedido.TIPO_RESGATE, null, PedidoEstado.CANCELADO, false, 4 * DAY);
            credito(ana, -1, MovimentoCredito.TIPO_RESERVA, cancelado, 4 * DAY);
            credito(ana, 1, MovimentoCredito.TIPO_LIBERACAO, cancelado, 3 * DAY);
            pedido(aAlgoritmos, bruno, carla, Pedido.TIPO_TROCA_DIRETA, ex1984Bruno, PedidoEstado.EXPIRADO, false, 5 * DAY);
            long disputa = pedido(aSertao, ana, diego, Pedido.TIPO_RESGATE, null, PedidoEstado.EM_DISPUTA, false, 3 * DAY);
            credito(ana, -1, MovimentoCredito.TIPO_RESERVA, disputa, 3 * DAY);
            // Ana: 2 - 1 (reserva congelada na disputa) = 1 disponível

            estante(ana, sertao, ItemEstante.STATUS_LENDO);
            estante(ana, capitaes, ItemEstante.STATUS_PASSADO_ADIANTE);
            estante(ana, horaEstrela, ItemEstante.STATUS_PASSADO_ADIANTE);
            estante(bruno, capitaes, ItemEstante.STATUS_LENDO);
            estante(carla, horaEstrela, ItemEstante.STATUS_LIDO);
            estante(diego, quimica, ItemEstante.STATUS_LENDO);
            desejo(bruno, algebra);
            desejo(carla, sapiens);
            desejo(diego, calculo);

            long p1 = post(ana, horaEstrela, PostMural.TIPO_TROCA_CONCLUIDA,
                    "Passei A Hora da Estrela adiante. Que chegue a quem precisa dele.", 12 * DAY);
            long p2 = post(bruno, calculo, PostMural.TIPO_PROCURO,
                    "Alguém tem o volume 1 do Stewart sobrando? Estou no 2º semestre.", 2 * DAY);
            long p3 = post(diego, fisica, PostMural.TIPO_DICA,
                    "Para Física I, os exercícios resolvidos do fim de cada capítulo valem mais que a teoria.", DAY);
            reacao(p1, carla, Reacao.TIPO_UTIL);
            reacao(p3, bruno, Reacao.TIPO_UTIL);
            reacao(p2, diego, Reacao.TIPO_QUERO);
        }

        private long usuario(String nome, String email, String curso, int semestre, String papel, String status) {
            Usuario u = new Usuario();
            u.nome = nome;
            u.email = email;
            u.curso = curso;
            u.semestre = semestre;
            u.papel = papel;
            u.status = status;
            u.senhaHash = PasswordHasher.hash(DEMO_PASSWORD.toCharArray());
            u.criadoEm = now - 40 * DAY;
            return db.usuarioDao().insert(u);
        }

        private long obra(String titulo, String autor) {
            Obra o = new Obra();
            o.titulo = titulo;
            o.autor = autor;
            return db.obraDao().insert(o);
        }

        private long disciplina(String nome, String curso) {
            Disciplina d = new Disciplina();
            d.nome = nome;
            d.curso = curso;
            return db.obraDao().insertDisciplina(d);
        }

        private void recomenda(long obraId, long disciplinaId) {
            ObraDisciplina link = new ObraDisciplina();
            link.obraId = obraId;
            link.disciplinaId = disciplinaId;
            db.obraDao().insertObraDisciplina(link);
        }

        private long exemplar(long obraId, long donoId, String edicao, String conservacao) {
            Exemplar e = new Exemplar();
            e.obraId = obraId;
            e.donoAtualId = donoId;
            e.edicao = edicao;
            e.conservacao = conservacao;
            long id = db.exemplarDao().insert(e);
            PosseExemplar p = new PosseExemplar();
            p.exemplarId = id;
            p.usuarioId = donoId;
            p.desde = now - 60 * DAY;
            db.exemplarDao().insertPosse(p);
            return id;
        }

        private long anuncio(long exemplarId, long donoId, String modalidade, long ago) {
            Anuncio a = new Anuncio();
            a.exemplarId = exemplarId;
            a.donoId = donoId;
            a.modalidade = modalidade;
            a.ativo = true;
            a.criadoEm = now - ago;
            return db.anuncioDao().insert(a);
        }

        private long anuncioInativo(long exemplarId, long donoId, long ago) {
            long id = anuncio(exemplarId, donoId, Anuncio.MODALIDADE_DOACAO, ago);
            Anuncio a = db.anuncioDao().findById(id);
            a.ativo = false;
            db.anuncioDao().update(a);
            return id;
        }

        private long pedido(long anuncioId, long solicitante, long dono, String tipo, Long oferecido,
                            String estado, boolean semCredito, long ago) {
            Pedido p = new Pedido();
            p.anuncioId = anuncioId;
            p.solicitanteId = solicitante;
            p.donoId = dono;
            p.tipo = tipo;
            p.exemplarOferecidoId = oferecido;
            p.estado = estado;
            p.semCredito = semCredito;
            p.criadoEm = now - ago;
            p.atualizadoEm = now - ago + HOUR;
            if (PedidoEstado.ACEITO.equals(estado) || PedidoEstado.EM_DISPUTA.equals(estado)
                    || PedidoEstado.CONCLUIDO.equals(estado)) {
                p.aceitoEm = now - ago + HOUR;
            }
            return db.pedidoDao().insert(p);
        }

        private void concluirResgate(long exemplarId, long anuncioId, long doador, long resgatador,
                                     boolean comCredito, long ago) {
            long pedidoId = pedido(anuncioId, resgatador, doador, Pedido.TIPO_RESGATE, null,
                    PedidoEstado.CONCLUIDO, !comCredito, ago);
            Pedido p = db.pedidoDao().findById(pedidoId);
            p.confirmadoDono = true;
            p.confirmadoSolicitante = true;
            p.atualizadoEm = now - ago + 2 * DAY;
            db.pedidoDao().update(p);
            if (comCredito) credito(resgatador, -1, MovimentoCredito.TIPO_RESERVA, pedidoId, ago);
            credito(doador, 1, MovimentoCredito.TIPO_DOACAO, pedidoId, ago - 2 * DAY);
            Exemplar e = db.exemplarDao().findById(exemplarId);
            e.donoAtualId = resgatador;
            db.exemplarDao().update(e);
            db.exemplarDao().closeCurrentPossession(exemplarId, now - ago + 2 * DAY);
            PosseExemplar posse = new PosseExemplar();
            posse.exemplarId = exemplarId;
            posse.usuarioId = resgatador;
            posse.desde = now - ago + 2 * DAY;
            posse.pedidoId = pedidoId;
            db.exemplarDao().insertPosse(posse);
        }

        private void credito(long usuarioId, int valor, String tipo, long pedidoId, long ago) {
            MovimentoCredito m = new MovimentoCredito();
            m.usuarioId = usuarioId;
            m.valor = valor;
            m.tipo = tipo;
            m.pedidoId = pedidoId;
            m.criadoEm = now - ago;
            db.creditoDao().insert(m);
        }

        private void mensagem(long pedidoId, long autor, String texto, boolean pronta, long ago) {
            Mensagem m = new Mensagem();
            m.pedidoId = pedidoId;
            m.autorId = autor;
            m.texto = texto;
            m.pronta = pronta;
            m.enviadaEm = now - ago;
            db.socialDao().insertMensagem(m);
        }

        /** Aviso ainda não entregue: aparece como notificação no próximo login da pessoa. */
        private void aviso(long usuarioId, String titulo, String texto, long pedidoId, long ago) {
            Notificacao n = new Notificacao();
            n.usuarioId = usuarioId;
            n.canal = "trocas";
            n.titulo = titulo;
            n.texto = texto;
            n.destinoTipo = Notificacao.DESTINO_PEDIDO;
            n.destinoId = pedidoId;
            n.criadaEm = now - ago;
            db.notificacaoDao().insert(n);
        }

        private void estante(long usuarioId, long obraId, String status) {
            ItemEstante i = new ItemEstante();
            i.usuarioId = usuarioId;
            i.obraId = obraId;
            i.status = status;
            i.atualizadoEm = now - DAY;
            db.socialDao().upsertEstante(i);
        }

        private void desejo(long usuarioId, long obraId) {
            Desejo d = new Desejo();
            d.usuarioId = usuarioId;
            d.obraId = obraId;
            d.criadoEm = now - 3 * DAY;
            db.socialDao().insertDesejo(d);
        }

        private long post(long autor, long obraId, String tipo, String texto, long ago) {
            PostMural p = new PostMural();
            p.autorId = autor;
            p.obraId = obraId;
            p.tipo = tipo;
            p.texto = texto;
            p.criadoEm = now - ago;
            return db.socialDao().insertPost(p);
        }

        private void reacao(long postId, long usuarioId, String tipo) {
            Reacao r = new Reacao();
            r.postId = postId;
            r.usuarioId = usuarioId;
            r.tipo = tipo;
            db.socialDao().insertReacao(r);
        }
    }
}
