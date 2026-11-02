package br.ufms.leiaufms.ui.listing;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.PontoEncontro;
import br.ufms.leiaufms.data.local.model.ListingRow;
import br.ufms.leiaufms.data.repository.ListingRepository;

/** Novo anúncio e edição (RF-12, RF-13). */
public class NewListingViewModel extends AndroidViewModel {

    public enum State { IDLE, LOADING, CREATED, SAVED, BLOCKED, ERROR }

    private final MutableLiveData<State> state = new MutableLiveData<>(State.IDLE);
    private final MutableLiveData<ListingRow> existing = new MutableLiveData<>();
    private final LiveData<List<PontoEncontro>> points;
    private final long userId;
    public Uri photo;
    public Double lat;
    public Double lng;
    public Long pontoId;
    private boolean loaded;

    public NewListingViewModel(@NonNull Application app) {
        super(app);
        userId = SessionManager.get(app).currentUserId();
        points = AppDatabase.get(app).pontoDao().observeActive();
    }

    public LiveData<State> state() {
        return state;
    }

    public LiveData<ListingRow> existing() {
        return existing;
    }

    public LiveData<List<PontoEncontro>> points() {
        return points;
    }

    public void consume() {
        state.setValue(State.IDLE);
    }

    public void load(long anuncioId) {
        if (loaded) return;
        loaded = true;
        AppExecutors.io().execute(() -> existing.postValue(AppDatabase.get(getApplication()).anuncioDao().row(anuncioId)));
    }

    public void save(long anuncioId, ListingRepository.Form form) {
        if (state.getValue() == State.LOADING) return;
        state.setValue(State.LOADING);
        AppExecutors.io().execute(() -> {
            try {
                ListingRepository repo = new ListingRepository(getApplication());
                if (anuncioId > 0) {
                    ListingRepository.Result r = repo.update(anuncioId, userId, form);
                    state.postValue(r == ListingRepository.Result.OK ? State.SAVED
                            : r == ListingRepository.Result.BLOCKED ? State.BLOCKED : State.ERROR);
                } else {
                    repo.create(userId, form);
                    state.postValue(State.CREATED);
                }
            } catch (Exception e) {
                state.postValue(State.ERROR);
            }
        });
    }
}
