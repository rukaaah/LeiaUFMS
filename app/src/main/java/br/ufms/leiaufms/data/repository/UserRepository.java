package br.ufms.leiaufms.data.repository;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.WorkerThread;

import java.io.IOException;

import br.ufms.leiaufms.core.media.ImageStore;
import br.ufms.leiaufms.core.security.PasswordHasher;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.core.util.Validators;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.Usuario;

/** Perfil (RF-06 a RF-10): dados, senha, foto e métricas do cartão de confiança. */
public class UserRepository {

    public enum UpdateResult { OK, EMAIL_TAKEN, NOT_FOUND }

    public enum PasswordResult { OK, WRONG_CURRENT }

    /** Métricas do cartão de confiança. Saldo só aparece no próprio perfil. */
    public static class Metrics {
        public int trades;
        public int donations;
        public Double rating;
        public int ratingCount;
        public int creditsAvailable;
        public int creditsReserved;
    }

    private final Context context;
    private final AppDatabase db;

    public UserRepository(Context context) {
        this.context = context.getApplicationContext();
        db = AppDatabase.get(context);
    }

    @WorkerThread
    public UpdateResult updateProfile(long id, String nome, String email, String curso, int semestre, String status) {
        Usuario u = db.usuarioDao().findById(id);
        if (u == null) return UpdateResult.NOT_FOUND;
        String normalized = Validators.normalizeEmail(email);
        if (db.usuarioDao().countEmail(normalized, id) > 0) return UpdateResult.EMAIL_TAKEN;
        u.nome = nome.trim();
        u.email = normalized;
        u.curso = curso.trim();
        u.semestre = semestre;
        String st = status == null ? "" : status.trim();
        u.status = st.isEmpty() ? null : st;
        db.usuarioDao().update(u);
        return UpdateResult.OK;
    }

    /** RF-08: confere a senha atual e grava um hash novo (salt novo). Encerra as outras sessões. */
    @WorkerThread
    public PasswordResult changePassword(long id, char[] atual, char[] nova) {
        Usuario u = db.usuarioDao().findById(id);
        if (u == null || !PasswordHasher.verify(atual, u.senhaHash)) {
            PasswordHasher.wipe(nova);
            return PasswordResult.WRONG_CURRENT;
        }
        db.usuarioDao().updatePasswordHash(id, PasswordHasher.hash(nova));
        SessionManager.get(context).revokeOtherSessions();
        return PasswordResult.OK;
    }

    /** RF-09/RF-10: salva a imagem em filesDir, grava o caminho e apaga a foto antiga. */
    @WorkerThread
    public boolean updatePhoto(long id, Uri source) {
        try {
            String path = ImageStore.save(context, source, "perfil_" + id);
            if (path == null) return false;
            Usuario u = db.usuarioDao().findById(id);
            String old = u == null ? null : u.fotoPath;
            db.usuarioDao().updatePhoto(id, path);
            ImageStore.delete(context, old);
            ImageStore.captureFile(context).delete();
            return true;
        } catch (IOException | SecurityException e) {
            return false;
        }
    }

    @WorkerThread
    public Metrics metrics(long id, boolean own) {
        Metrics m = new Metrics();
        m.trades = db.pedidoDao().countConcluded(id);
        m.donations = db.pedidoDao().countDonations(id);
        m.rating = db.socialDao().averageRating(id);
        m.ratingCount = db.socialDao().ratingCount(id);
        if (own) {
            m.creditsAvailable = db.creditoDao().available(id);
            m.creditsReserved = db.creditoDao().reserved(id);
        }
        return m;
    }
}
