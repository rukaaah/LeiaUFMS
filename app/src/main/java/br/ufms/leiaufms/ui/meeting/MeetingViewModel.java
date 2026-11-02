package br.ufms.leiaufms.ui.meeting;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import br.ufms.leiaufms.AppConfig;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.Pedido;
import br.ufms.leiaufms.data.local.entity.PontoEncontro;
import br.ufms.leiaufms.data.repository.MeetingRepository;
import br.ufms.leiaufms.data.repository.TradeRepository;

/** Estado de "Marcar encontro" e do cadastro de pontos (RF-21, RF-29, RF-30). */
public class MeetingViewModel extends AndroidViewModel {

    private final long userId;
    private final LiveData<List<PontoEncontro>> points;
    private final MutableLiveData<PontoEncontro> selection = new MutableLiveData<>();
    private final MutableLiveData<Long> when = new MutableLiveData<>();
    private final MutableLiveData<String> events = new MutableLiveData<>();
    PontoEncontro selected;
    private long pedidoId = -1;
    private volatile long deadline = System.currentTimeMillis() + AppConfig.DELIVERY_DEADLINE_MS;
    private Long dateUtc;
    private int hour = 12;
    private int minute = 0;
    private boolean timeChosen;

    public MeetingViewModel(@NonNull Application app) {
        super(app);
        userId = SessionManager.get(app).currentUserId();
        points = AppDatabase.get(app).pontoDao().observeActive();
    }

    public void init(long id) {
        if (pedidoId == id || id <= 0) return;
        pedidoId = id;
        AppExecutors.io().execute(() -> {
            Pedido p = AppDatabase.get(getApplication()).pedidoDao().findById(id);
            if (p == null) return;
            long base = p.aceitoEm == null ? p.atualizadoEm : p.aceitoEm;
            deadline = base + AppConfig.DELIVERY_DEADLINE_MS;
            if (p.pontoEncontroId != null) {
                PontoEncontro ponto = AppDatabase.get(getApplication()).pontoDao().findById(p.pontoEncontroId);
                if (ponto != null) {
                    selected = ponto;
                    selection.postValue(ponto);
                }
            }
        });
    }

    public LiveData<List<PontoEncontro>> points() {
        return points;
    }

    public LiveData<PontoEncontro> selection() {
        return selection;
    }

    public LiveData<Long> when() {
        return when;
    }

    public LiveData<String> events() {
        return events;
    }

    public void consume() {
        events.setValue(null);
    }

    public long deadline() {
        return deadline;
    }

    public void select(PontoEncontro p) {
        selected = p;
        selection.setValue(p);
    }

    public Long dateUtc() {
        return dateUtc;
    }

    public int hour() {
        return hour;
    }

    public int minute() {
        return minute;
    }

    public boolean timeChosen() {
        return timeChosen;
    }

    public void setDateUtc(Long utc) {
        dateUtc = utc;
        when.setValue(meetingTime());
    }

    public void setTime(int h, int m) {
        hour = h;
        minute = m;
        timeChosen = true;
        when.setValue(meetingTime());
    }

    /** Junta a data escolhida (UTC do calendário) com a hora local. */
    public Long meetingTime() {
        if (dateUtc == null || !timeChosen) return null;
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.setTimeInMillis(dateUtc);
        Calendar local = Calendar.getInstance();
        local.clear();
        local.set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), hour, minute);
        return local.getTimeInMillis();
    }

    public void schedule() {
        Long t = meetingTime();
        PontoEncontro p = selected;
        if (t == null || p == null) return;
        AppExecutors.io().execute(() -> {
            TradeRepository.Result r = new TradeRepository(getApplication()).scheduleMeeting(pedidoId, userId, p.id, t);
            events.postValue(r == TradeRepository.Result.OK ? "scheduled"
                    : r == TradeRepository.Result.BAD_DATE ? "bad_date" : "error");
        });
    }

    public void addPoint(String nome, String descricao, double lat, double lng) {
        AppExecutors.io().execute(() -> {
            long id = new MeetingRepository(getApplication()).addPoint(userId, nome, descricao, lat, lng);
            events.postValue(id > 0 ? "point_saved" : "error");
        });
    }

    public void removePoint(long id) {
        AppExecutors.io().execute(() ->
                events.postValue(new MeetingRepository(getApplication()).removePoint(userId, id) ? "point_removed" : "error"));
    }
}
