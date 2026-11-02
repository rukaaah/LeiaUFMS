package br.ufms.leiaufms.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.data.local.dao.AnuncioDao;
import br.ufms.leiaufms.data.local.dao.CreditoDao;
import br.ufms.leiaufms.data.local.dao.ExemplarDao;
import br.ufms.leiaufms.data.local.dao.NotificacaoDao;
import br.ufms.leiaufms.data.local.dao.ObraDao;
import br.ufms.leiaufms.data.local.dao.PedidoDao;
import br.ufms.leiaufms.data.local.dao.PontoDao;
import br.ufms.leiaufms.data.local.dao.SessaoDao;
import br.ufms.leiaufms.data.local.dao.SocialDao;
import br.ufms.leiaufms.data.local.dao.UsuarioDao;
import br.ufms.leiaufms.data.local.entity.Anuncio;
import br.ufms.leiaufms.data.local.entity.Avaliacao;
import br.ufms.leiaufms.data.local.entity.Desejo;
import br.ufms.leiaufms.data.local.entity.Disciplina;
import br.ufms.leiaufms.data.local.entity.EdicaoCompativel;
import br.ufms.leiaufms.data.local.entity.Exemplar;
import br.ufms.leiaufms.data.local.entity.ItemEstante;
import br.ufms.leiaufms.data.local.entity.Mensagem;
import br.ufms.leiaufms.data.local.entity.MovimentoCredito;
import br.ufms.leiaufms.data.local.entity.Notificacao;
import br.ufms.leiaufms.data.local.entity.Obra;
import br.ufms.leiaufms.data.local.entity.ObraDisciplina;
import br.ufms.leiaufms.data.local.entity.Pedido;
import br.ufms.leiaufms.data.local.entity.PontoEncontro;
import br.ufms.leiaufms.data.local.entity.PosseExemplar;
import br.ufms.leiaufms.data.local.entity.PostMural;
import br.ufms.leiaufms.data.local.entity.Reacao;
import br.ufms.leiaufms.data.local.entity.Sessao;
import br.ufms.leiaufms.data.local.entity.Usuario;

/** Banco local do LeiaUFMS (ADR-01). Entidades em docs/04-arquitetura.md §4. */
@Database(entities = {
        Usuario.class, Sessao.class, Obra.class, EdicaoCompativel.class, Disciplina.class,
        ObraDisciplina.class, Exemplar.class, PosseExemplar.class, Anuncio.class, Pedido.class,
        Mensagem.class, MovimentoCredito.class, Avaliacao.class, ItemEstante.class, Desejo.class,
        PostMural.class, Reacao.class, PontoEncontro.class, Notificacao.class
}, version = 1, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {

    private static final String NAME = "leiaufms.db";
    private static volatile AppDatabase instance;

    public abstract UsuarioDao usuarioDao();

    public abstract SessaoDao sessaoDao();

    public abstract ObraDao obraDao();

    public abstract ExemplarDao exemplarDao();

    public abstract AnuncioDao anuncioDao();

    public abstract PedidoDao pedidoDao();

    public abstract CreditoDao creditoDao();

    public abstract SocialDao socialDao();

    public abstract PontoDao pontoDao();

    public abstract NotificacaoDao notificacaoDao();

    public static AppDatabase get(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    Context app = context.getApplicationContext();
                    instance = Room.databaseBuilder(app, AppDatabase.class, NAME)
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    // O seed usa os DAOs, então roda depois que o banco termina de abrir.
                                    AppExecutors.io().execute(() -> SeedData.populate(get(app)));
                                }
                            })
                            .build();
                }
            }
        }
        return instance;
    }
}
