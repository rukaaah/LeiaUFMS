package br.ufms.leiaufms.ui.listing;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.Pedido;
import br.ufms.leiaufms.data.local.model.ExemplarRow;
import br.ufms.leiaufms.data.local.model.ListingRow;
import br.ufms.leiaufms.data.repository.ListingRepository;
import br.ufms.leiaufms.data.repository.TradeRepository;
import br.ufms.leiaufms.data.repository.UserRepository;

/** Detalhe do anúncio e pedidos de troca/resgate (RF-18, RF-19, RF-28). */
public class ListingDetailViewModel extends AndroidViewModel {

    /** Estado complementar ao anúncio: pedido ativo, métricas do dono, saldo e se há pedido aceito. */
    public static class Extra {
        public Pedido myActive;
        public UserRepository.Metrics ownerMetrics;
        public int myCredits;
        public boolean reserved;
        public int wishCount;
        public int hands;
        public boolean wished;
        public List<br.ufms.leiaufms.data.local.entity.Disciplina> courses;
        public List<br.ufms.leiaufms.data.local.dao.ExemplarDao.TrajetoriaRow> trajectory;
    }

    public static class Event {
        public final String kind;
        public final Object value;

        Event(String kind, Object value) {
            this.kind = kind;
            this.value = value;
        }
    }

    private final AppDatabase db;
    private final long userId;
    private final MutableLiveData<Extra> extra = new MutableLiveData<>();
    private final MutableLiveData<Event> events = new MutableLiveData<>();
    private LiveData<ListingRow> row;
    private long anuncioId = -1;

    public ListingDetailViewModel(@NonNull Application app) {
        super(app);
        db = AppDatabase.get(app);
        userId = SessionManager.get(app).currentUserId();
    }

    public void init(long id) {
        if (anuncioId == id) return;
        anuncioId = id;
        row = db.anuncioDao().observeRow(id);
    }

    public long userId() {
        return userId;
    }

    public LiveData<ListingRow> row() {
        return row;
    }

    public LiveData<Extra> extra() {
        return extra;
    }

    public LiveData<Event> events() {
        return events;
    }

    public void consume() {
        events.setValue(null);
    }

    public void refresh(long donoId) {
        long id = anuncioId;
        AppExecutors.io().execute(() -> {
            Extra e = new Extra();
            e.myActive = db.pedidoDao().activeFor(id, userId);
            e.ownerMetrics = new UserRepository(getApplication()).metrics(donoId, false);
            e.myCredits = db.creditoDao().available(userId);
            e.reserved = db.anuncioDao().countAccepted(id) > 0;
            ListingRow r = db.anuncioDao().row(id);
            if (r != null) {
                e.wished = db.socialDao().hasWish(userId, r.obraId) > 0;
                e.wishCount = db.socialDao().wishCount(r.obraId) - (e.wished ? 1 : 0);
                e.hands = db.exemplarDao().hands(r.exemplarId);
                e.courses = db.obraDao().disciplinaRows(r.obraId);
                e.trajectory = db.exemplarDao().trajectory(r.exemplarId);
            }
            extra.postValue(e);
        });
    }

    public LiveData<List<ListingRow>> copies(long obraId) {
        return db.anuncioDao().byObra(obraId);
    }

    public void toggleWish(long obraId, long donoId) {
        AppExecutors.io().execute(() -> {
            new br.ufms.leiaufms.data.repository.SocialRepository(getApplication()).toggleWish(userId, obraId);
            refreshSync(donoId);
        });
    }

    private void refreshSync(long donoId) {
        refresh(donoId);
    }

    public void loadOfferable() {
        AppExecutors.io().execute(() -> {
            List<ExemplarRow> list = db.exemplarDao().offerable(userId);
            events.postValue(new Event("offerable", list));
        });
    }

    public void request(String tipo, Long exemplarId) {
        long id = anuncioId;
        AppExecutors.io().execute(() -> {
            long[] out = new long[1];
            TradeRepository.Result r;
            try {
                r = new TradeRepository(getApplication()).request(id, userId, tipo, exemplarId, out);
            } catch (RuntimeException ex) {
                r = null;
            }
            events.postValue(new Event("request", r));
        });
    }

    public void close() {
        long id = anuncioId;
        AppExecutors.io().execute(() ->
                events.postValue(new Event("close", new ListingRepository(getApplication()).close(id, userId))));
    }
}
