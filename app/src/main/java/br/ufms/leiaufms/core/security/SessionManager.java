package br.ufms.leiaufms.core.security;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.WorkerThread;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import br.ufms.leiaufms.AppConfig;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.Sessao;

/**
 * Sessão ativa (RF-04, RF-05, ADR-04). O SharedPreferences guarda só o id do usuário e um token
 * aleatório; o Room guarda o hash SHA-256 do token e a expiração (30 dias). Logout apaga as duas pontas.
 * Métodos marcados com @WorkerThread acessam o banco e nunca rodam na main thread.
 */
public final class SessionManager {

    public enum Status { VALID, NONE, EXPIRED }

    private static final String PREFS = "leia_sessao";
    private static final String KEY_USER = "usuario_id";
    private static final String KEY_TOKEN = "token";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static volatile SessionManager instance;
    private static volatile long currentUserId = -1;

    private final SharedPreferences prefs;
    private final AppDatabase db;

    private SessionManager(Context context) {
        Context app = context.getApplicationContext();
        prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        db = AppDatabase.get(app);
    }

    public static SessionManager get(Context context) {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) instance = new SessionManager(context);
            }
        }
        return instance;
    }

    /** Usuário da sessão já validada nesta execução do processo, ou -1. */
    public long currentUserId() {
        return currentUserId;
    }

    public boolean hasValidatedSession() {
        return currentUserId > 0;
    }

    @WorkerThread
    public void start(long usuarioId) {
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        long now = System.currentTimeMillis();
        Sessao s = new Sessao();
        s.usuarioId = usuarioId;
        s.tokenHash = sha256(token);
        s.criadaEm = now;
        s.expiraEm = now + AppConfig.SESSION_DURATION_MS;
        db.sessaoDao().deleteExpired(now);
        db.sessaoDao().insert(s);
        prefs.edit().putLong(KEY_USER, usuarioId).putString(KEY_TOKEN, token).commit();
        currentUserId = usuarioId;
    }

    @WorkerThread
    public Status validate() {
        long usuarioId = prefs.getLong(KEY_USER, -1);
        String token = prefs.getString(KEY_TOKEN, null);
        if (usuarioId <= 0 || token == null) {
            currentUserId = -1;
            return Status.NONE;
        }
        Sessao s = db.sessaoDao().find(sha256(token), usuarioId);
        if (s == null || db.usuarioDao().findById(usuarioId) == null) {
            clearLocal();
            return Status.NONE;
        }
        if (s.expiraEm < System.currentTimeMillis()) {
            db.sessaoDao().deleteByToken(s.tokenHash);
            clearLocal();
            return Status.EXPIRED;
        }
        currentUserId = usuarioId;
        return Status.VALID;
    }

    @WorkerThread
    public void logout() {
        String token = prefs.getString(KEY_TOKEN, null);
        if (token != null) db.sessaoDao().deleteByToken(sha256(token));
        clearLocal();
    }

    /** Depois de trocar a senha, derruba as outras sessões do usuário (RF-08). */
    @WorkerThread
    public void revokeOtherSessions() {
        String token = prefs.getString(KEY_TOKEN, null);
        long usuarioId = prefs.getLong(KEY_USER, -1);
        if (token != null && usuarioId > 0) db.sessaoDao().deleteOthers(usuarioId, sha256(token));
    }

    private void clearLocal() {
        prefs.edit().clear().commit();
        currentUserId = -1;
    }

    static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
