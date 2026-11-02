package br.ufms.leiaufms.ui.profile;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.Usuario;
import br.ufms.leiaufms.data.repository.UserRepository;

/** Perfil próprio ou de outro aluno (RF-06), foto (RF-09, RF-10). */
public class ProfileViewModel extends AndroidViewModel {

    public enum PhotoState { IDLE, SAVING, SAVED, ERROR }

    private final UserRepository repository;
    private final MutableLiveData<UserRepository.Metrics> metrics = new MutableLiveData<>();
    private final MutableLiveData<PhotoState> photoState = new MutableLiveData<>(PhotoState.IDLE);
    private LiveData<Usuario> user;
    private long userId = -1;
    private boolean own;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    public void init(long userId, boolean own) {
        if (this.userId == userId) return;
        this.userId = userId;
        this.own = own;
        user = AppDatabase.get(getApplication()).usuarioDao().observe(userId);
    }

    public LiveData<Usuario> user() {
        return user;
    }

    public LiveData<UserRepository.Metrics> metrics() {
        return metrics;
    }

    public LiveData<PhotoState> photoState() {
        return photoState;
    }

    public void refreshMetrics() {
        long id = userId;
        boolean isOwn = own;
        AppExecutors.io().execute(() -> metrics.postValue(repository.metrics(id, isOwn)));
    }

    public void savePhoto(Uri uri) {
        photoState.setValue(PhotoState.SAVING);
        long id = userId;
        AppExecutors.io().execute(() ->
                photoState.postValue(repository.updatePhoto(id, uri) ? PhotoState.SAVED : PhotoState.ERROR));
    }

    public void consumePhotoState() {
        photoState.setValue(PhotoState.IDLE);
    }
}
