package br.ufms.leiaufms.ui.home;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import br.ufms.leiaufms.core.location.WalkEstimate;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.Usuario;
import br.ufms.leiaufms.data.local.model.ListingRow;
import br.ufms.leiaufms.data.local.model.PedidoRow;
import br.ufms.leiaufms.data.local.model.ShelfRow;
import br.ufms.leiaufms.data.repository.SocialRepository;

/**
 * Início (briefing v2): "O que tem pra mim agora?". Junta pedidos que pedem ação, desejos que
 * apareceram, livros a pé, livros do curso e os que chegaram agora.
 */
public class HomeViewModel extends AndroidViewModel {

    private final AppDatabase db;
    public final long userId;
    private final MutableLiveData<Location> location = new MutableLiveData<>();
    private final LiveData<List<ListingRow>> all;
    private final MediatorLiveData<List<ListingRow>> near = new MediatorLiveData<>();
    private final MediatorLiveData<List<ListingRow>> fresh = new MediatorLiveData<>();
    private final LiveData<List<ListingRow>> course;

    public HomeViewModel(@NonNull Application app) {
        super(app);
        db = AppDatabase.get(app);
        userId = SessionManager.get(app).currentUserId();
        all = db.anuncioDao().search("", "");
        near.addSource(all, l -> computeNear());
        near.addSource(location, l -> computeNear());
        fresh.addSource(all, l -> computeFresh());
        fresh.addSource(location, l -> computeFresh());
        course = Transformations.switchMap(user(), u -> db.anuncioDao().forCourse(userId, courseKey(u)));
    }

    static String courseKey(Usuario u) {
        if (u == null || u.curso == null || u.curso.trim().isEmpty()) return "\u0000";
        return u.curso.trim().split("\\s+")[0];
    }

    public LiveData<Usuario> user() {
        return db.usuarioDao().observe(userId);
    }

    public LiveData<Integer> credits() {
        return db.creditoDao().observeAvailable(userId);
    }

    public LiveData<List<ListingRow>> near() {
        return near;
    }

    public LiveData<List<ListingRow>> fresh() {
        return fresh;
    }

    public LiveData<List<ListingRow>> course() {
        return course;
    }

    public LiveData<ShelfRow> lastRead() {
        return db.socialDao().lastRead(userId);
    }

    public LiveData<List<ListingRow>> because(long obraId) {
        return db.anuncioDao().because(userId, obraId);
    }

    public LiveData<String> topGenre() {
        return db.anuncioDao().topGenre(userId);
    }

    public LiveData<List<ListingRow>> byGenre(String genero) {
        return db.anuncioDao().byGenre(userId, genero);
    }

    public LiveData<List<Long>> wished() {
        return db.socialDao().wishedObras(userId);
    }

    public LiveData<List<PedidoRow>> received() {
        return db.pedidoDao().received(userId);
    }

    public LiveData<List<PedidoRow>> sent() {
        return db.pedidoDao().sent(userId);
    }

    public LiveData<List<ShelfRow>> wishes() {
        return db.socialDao().wishes(userId);
    }

    public boolean hasLocation() {
        return location.getValue() != null;
    }

    public void setLocation(Location l) {
        location.setValue(l);
    }

    public void toggleWish(long obraId) {
        AppExecutors.io().execute(() -> new SocialRepository(getApplication()).toggleWish(userId, obraId));
    }

    private List<ListingRow> withDistances() {
        List<ListingRow> raw = all.getValue();
        List<ListingRow> out = new ArrayList<>();
        if (raw == null) return out;
        Location l = location.getValue();
        for (ListingRow original : raw) {
            if (original.donoId == userId) continue;
            ListingRow r = original.copy();
            Double lat = r.latitude();
            Double lng = r.longitude();
            if (l != null && lat != null && lng != null) {
                r.distancia = WalkEstimate.distance(l.getLatitude(), l.getLongitude(), lat, lng);
            }
            out.add(r);
        }
        return out;
    }

    private void computeNear() {
        List<ListingRow> out = new ArrayList<>();
        for (ListingRow r : withDistances()) {
            if (r.distancia != null && WalkEstimate.isWalkable(r.distancia)) out.add(r);
        }
        Collections.sort(out, Comparator.comparing((ListingRow r) -> r.distancia));
        near.setValue(out);
    }

    private void computeFresh() {
        List<ListingRow> list = withDistances();
        fresh.setValue(list.size() > 10 ? new ArrayList<>(list.subList(0, 10)) : list);
    }
}
