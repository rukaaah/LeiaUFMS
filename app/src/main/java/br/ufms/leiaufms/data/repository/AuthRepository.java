package br.ufms.leiaufms.data.repository;

import android.content.Context;

import androidx.annotation.WorkerThread;

import br.ufms.leiaufms.core.notification.Notifier;
import br.ufms.leiaufms.core.security.PasswordHasher;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.core.util.Validators;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.Usuario;

/** Cadastro e login (RF-02, RF-03). Senhas chegam como char[] e são zeradas no PasswordHasher. */
public class AuthRepository {

    public enum RegisterResult { OK, EMAIL_TAKEN }

    private final Context context;
    private final AppDatabase db;
    private final SessionManager session;

    public AuthRepository(Context context) {
        this.context = context.getApplicationContext();
        db = AppDatabase.get(context);
        session = SessionManager.get(context);
    }

    @WorkerThread
    public RegisterResult register(String nome, String email, String curso, int semestre, char[] senha) {
        AppDatabase.awaitReady();
        String normalized = Validators.normalizeEmail(email);
        if (db.usuarioDao().findByEmail(normalized) != null) {
            PasswordHasher.wipe(senha);
            return RegisterResult.EMAIL_TAKEN;
        }
        Usuario u = new Usuario();
        u.nome = nome.trim();
        u.email = normalized;
        u.curso = curso.trim();
        u.semestre = semestre;
        u.papel = Usuario.PAPEL_ALUNO;
        u.criadoEm = System.currentTimeMillis();
        u.senhaHash = PasswordHasher.hash(senha);
        long id;
        try {
            id = db.usuarioDao().insert(u);
        } catch (android.database.sqlite.SQLiteConstraintException e) {
            return RegisterResult.EMAIL_TAKEN;
        }
        session.start(id);
        return RegisterResult.OK;
    }

    /** Devolve true se as credenciais conferem. Mensagem única para e-mail ou senha errados. */
    @WorkerThread
    public boolean login(String email, char[] senha) {
        AppDatabase.awaitReady();
        Usuario u = db.usuarioDao().findByEmail(Validators.normalizeEmail(email));
        if (u == null) {
            // Gasta o mesmo tempo de um BCrypt para não revelar se o e-mail existe
            PasswordHasher.verify(senha, Dummy.HASH);
            return false;
        }
        if (!PasswordHasher.verify(senha, u.senhaHash)) return false;
        session.start(u.id);
        Notifier.deliverPending(context, u.id);
        return true;
    }

    /** Hash de uma senha aleatória descartada, usado só para igualar o tempo de resposta. */
    private static final class Dummy {
        static final String HASH = PasswordHasher.hash(
                java.util.UUID.randomUUID().toString().toCharArray());
    }
}
