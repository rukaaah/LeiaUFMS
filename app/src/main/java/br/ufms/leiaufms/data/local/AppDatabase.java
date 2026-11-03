package br.ufms.leiaufms.data.local;

import android.content.Context;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.data.local.dao.AnuncioDao;
import br.ufms.leiaufms.data.local.dao.CreditoDao;
import br.ufms.leiaufms.data.local.dao.ExemplarDao;
import br.ufms.leiaufms.data.local.dao.LocalizacaoDao;
import br.ufms.leiaufms.data.local.dao.NotificacaoDao;
import br.ufms.leiaufms.data.local.dao.ObraDao;
import br.ufms.leiaufms.data.local.dao.PedidoDao;
import br.ufms.leiaufms.data.local.dao.PontoDao;
import br.ufms.leiaufms.data.local.dao.SessaoDao;
import br.ufms.leiaufms.data.local.dao.SocialDao;
import br.ufms.leiaufms.data.local.dao.UsuarioDao;
import br.ufms.leiaufms.data.local.entity.Anuncio;
import br.ufms.leiaufms.data.local.entity.Avaliacao;
import br.ufms.leiaufms.data.local.entity.ComentarioPost;
import br.ufms.leiaufms.data.local.entity.Desejo;
import br.ufms.leiaufms.data.local.entity.Disciplina;
import br.ufms.leiaufms.data.local.entity.EdicaoCompativel;
import br.ufms.leiaufms.data.local.entity.Exemplar;
import br.ufms.leiaufms.data.local.entity.ItemEstante;
import br.ufms.leiaufms.data.local.entity.Mensagem;
import br.ufms.leiaufms.data.local.entity.LocalizacaoEncontro;
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
        PostMural.class, Reacao.class, PontoEncontro.class, Notificacao.class, LocalizacaoEncontro.class,
        ComentarioPost.class
}, version = 6, exportSchema = true)
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

    public abstract LocalizacaoDao localizacaoDao();

    /** v2: lembrete de "encontro amanhã" (RF-33). */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE pedido ADD COLUMN lembreteEncontroEnviado INTEGER NOT NULL DEFAULT 0");
        }
    };

    /** v3: capa da obra vinda da Open Library. */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE obra ADD COLUMN capaPath TEXT");
            db.execSQL("ALTER TABLE obra ADD COLUMN capaTentativaEm INTEGER");
        }
    };

    /** v4: localização compartilhada no encontro (RF-41). */
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `localizacao_encontro` (`pedidoId` INTEGER NOT NULL, "
                    + "`usuarioId` INTEGER NOT NULL, `lat` REAL NOT NULL, `lng` REAL NOT NULL, "
                    + "`atualizadaEm` INTEGER NOT NULL, PRIMARY KEY(`pedidoId`, `usuarioId`))");
        }
    };

    /** v5: gênero da obra (categorias do Início). Obras já existentes ficam sem gênero. */
    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE obra ADD COLUMN genero TEXT");
        }
    };

    /**
     * v6: encontro vira proposta (RF-44) e comentários no mural (RF-43).
     * Encontros já marcados antes da v6 contam como aceitos.
     */
    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE pedido ADD COLUMN encontroPropostoPor INTEGER");
            db.execSQL("ALTER TABLE pedido ADD COLUMN encontroAceito INTEGER NOT NULL DEFAULT 0");
            db.execSQL("UPDATE pedido SET encontroAceito = 1 WHERE encontroEm IS NOT NULL");
            db.execSQL("CREATE TABLE IF NOT EXISTS `comentario_post` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "`postId` INTEGER NOT NULL, `autorId` INTEGER NOT NULL, `texto` TEXT NOT NULL, `criadoEm` INTEGER NOT NULL)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_comentario_post_postId` ON `comentario_post` (`postId`)");
        }
    };

    private static final CountDownLatch SEED_READY = new CountDownLatch(1);

    /** Abre o banco e cria os dados de demonstração na primeira execução (em background). */
    public static void prepare(Context context) {
        AppExecutors.io().execute(() -> {
            try {
                SeedData.populate(get(context));
            } finally {
                SEED_READY.countDown();
            }
        });
    }

    /** Login e cadastro esperam o seed terminar para não consultar um banco ainda vazio. */
    @WorkerThread
    public static void awaitReady() {
        try {
            SEED_READY.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static AppDatabase get(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    Context app = context.getApplicationContext();
                    instance = Room.databaseBuilder(app, AppDatabase.class, NAME)
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                            .build();
                }
            }
        }
        return instance;
    }
}
