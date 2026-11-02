package br.ufms.leiaufms.data.repository;

import android.content.Context;

import androidx.annotation.WorkerThread;

import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.PontoEncontro;
import br.ufms.leiaufms.data.local.entity.Usuario;

/** Pontos seguros (RF-30): só o Moderador cria e remove; coordenadas vêm do toque no mapa. */
public class MeetingRepository {

    private final AppDatabase db;

    public MeetingRepository(Context context) {
        db = AppDatabase.get(context);
    }

    @WorkerThread
    public boolean isModerator(long usuarioId) {
        Usuario u = db.usuarioDao().findById(usuarioId);
        return u != null && u.isModerador();
    }

    @WorkerThread
    public long addPoint(long moderadorId, String nome, String descricao, double lat, double lng) {
        if (!isModerator(moderadorId)) return -1;
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180 || nome == null || nome.trim().isEmpty()) return -1;
        PontoEncontro p = new PontoEncontro();
        p.nome = nome.trim();
        p.descricao = descricao == null || descricao.trim().isEmpty() ? null : descricao.trim();
        p.lat = lat;
        p.lng = lng;
        p.ativo = true;
        p.criadoPor = moderadorId;
        p.criadoEm = System.currentTimeMillis();
        return db.pontoDao().insert(p);
    }

    @WorkerThread
    public boolean removePoint(long moderadorId, long pontoId) {
        if (!isModerator(moderadorId)) return false;
        db.pontoDao().deactivate(pontoId);
        return true;
    }
}
