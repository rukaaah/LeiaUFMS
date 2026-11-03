package br.ufms.leiaufms.ui.trade;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.Avaliacao;
import br.ufms.leiaufms.data.local.entity.Usuario;
import br.ufms.leiaufms.data.local.model.PedidoRow;
import br.ufms.leiaufms.data.repository.SocialRepository;
import br.ufms.leiaufms.data.repository.TradeRepository;
import br.ufms.leiaufms.data.repository.UserRepository;

/** Detalhe do pedido e as ações da máquina de estados (RF-20 a RF-23). */
public class TradeDetailViewModel extends AndroidViewModel {

    /** Dados da outra parte: perfil, métricas e, para o dono num resgate, o saldo do solicitante. */
    public static class Party {
        public Usuario user;
        public UserRepository.Metrics metrics;
        public int requesterCredits;
    }

    private final AppDatabase db;
    private final long userId;
    private final MutableLiveData<Party> party = new MutableLiveData<>();
    private final MutableLiveData<TradeRepository.Result[]> result = new MutableLiveData<>();
    private LiveData<PedidoRow> row;
    private long pedidoId = -1;

    public TradeDetailViewModel(@NonNull Application app) {
        super(app);
        db = AppDatabase.get(app);
        userId = SessionManager.get(app).currentUserId();
    }

    public void init(long id) {
        if (pedidoId == id) return;
        pedidoId = id;
        row = db.pedidoDao().observeRow(id);
    }

    public long userId() {
        return userId;
    }

    public LiveData<PedidoRow> row() {
        return row;
    }

    public LiveData<Party> party() {
        return party;
    }

    /** [0] = resultado, consumido pela tela com {@link #consume()}. Ação guardada em actionName. */
    public LiveData<TradeRepository.Result[]> result() {
        return result;
    }

    public String actionName;

    public void consume() {
        result.setValue(null);
    }

    public void loadParty(long otherId, long solicitanteId) {
        AppExecutors.io().execute(() -> {
            Party p = new Party();
            p.user = db.usuarioDao().findById(otherId);
            p.metrics = new UserRepository(getApplication()).metrics(otherId, false);
            p.requesterCredits = db.creditoDao().available(solicitanteId)
                    + db.creditoDao().reserved(solicitanteId);
            party.postValue(p);
        });
    }

    public void run(String action) {
        actionName = action;
        long id = pedidoId;
        AppExecutors.io().execute(() -> {
            TradeRepository repo = new TradeRepository(getApplication());
            TradeRepository.Result r;
            try {
                switch (action) {
                    case "accept":
                        r = repo.accept(id, userId);
                        break;
                    case "decline":
                        r = repo.decline(id, userId);
                        break;
                    case "confirm":
                        r = repo.confirm(id, userId);
                        break;
                    default:
                        r = TradeRepository.Result.INVALID_STATE;
                }
            } catch (RuntimeException e) {
                r = TradeRepository.Result.INVALID_STATE;
            }
            result.postValue(new TradeRepository.Result[]{r});
        });
    }

    private final MutableLiveData<Avaliacao> myRating = new MutableLiveData<>();

    public LiveData<Avaliacao> myRating() {
        return myRating;
    }

    public void loadMyRating() {
        long id = pedidoId;
        AppExecutors.io().execute(() -> myRating.postValue(new SocialRepository(getApplication()).myRating(id, userId)));
    }

    /** RF-25: grava a nota e recarrega. O resultado volta como OK ou INVALID_STATE. */
    public void rate(int nota) {
        actionName = "rate";
        long id = pedidoId;
        AppExecutors.io().execute(() -> {
            SocialRepository repo = new SocialRepository(getApplication());
            SocialRepository.Result r = repo.rate(id, userId, nota);
            myRating.postValue(repo.myRating(id, userId));
            result.postValue(new TradeRepository.Result[]{r == SocialRepository.Result.OK
                    ? TradeRepository.Result.OK : TradeRepository.Result.INVALID_STATE});
        });
    }

    private final MutableLiveData<Boolean> moderator = new MutableLiveData<>(false);

    public LiveData<Boolean> moderator() {
        AppExecutors.io().execute(() -> {
            Usuario u = db.usuarioDao().findById(userId);
            moderator.postValue(u != null && u.isModerador());
        });
        return moderator;
    }

    public void dispute(String motivo) {
        actionName = "dispute";
        long id = pedidoId;
        AppExecutors.io().execute(() -> result.postValue(new TradeRepository.Result[]{
                new TradeRepository(getApplication()).openDispute(id, userId, motivo)}));
    }

    public void resolve(boolean concluir) {
        actionName = "resolve";
        long id = pedidoId;
        AppExecutors.io().execute(() -> result.postValue(new TradeRepository.Result[]{
                new TradeRepository(getApplication()).resolveDispute(id, userId, concluir)}));
    }

    public void cancel(String motivo) {
        actionName = "cancel";
        long id = pedidoId;
        AppExecutors.io().execute(() -> {
            TradeRepository.Result r = new TradeRepository(getApplication()).cancel(id, userId, motivo);
            result.postValue(new TradeRepository.Result[]{r});
        });
    }
}
