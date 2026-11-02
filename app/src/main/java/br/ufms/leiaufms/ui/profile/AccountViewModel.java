package br.ufms.leiaufms.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import br.ufms.leiaufms.core.security.PasswordHasher;
import br.ufms.leiaufms.core.security.SessionManager;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.data.local.entity.Usuario;
import br.ufms.leiaufms.data.repository.UserRepository;

/** Editar perfil (RF-07) e alterar senha (RF-08). */
public class AccountViewModel extends AndroidViewModel {

    public enum State { IDLE, LOADING, SAVED, EMAIL_TAKEN, WRONG_CURRENT, ERROR }

    private final UserRepository repository;
    private final MutableLiveData<State> state = new MutableLiveData<>(State.IDLE);
    private final MutableLiveData<Usuario> user = new MutableLiveData<>();
    private final long userId;

    public AccountViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
        userId = SessionManager.get(application).currentUserId();
    }

    public LiveData<State> state() {
        return state;
    }

    public LiveData<Usuario> user() {
        return user;
    }

    public void load() {
        if (user.getValue() != null) return;
        AppExecutors.io().execute(() -> user.postValue(AppDatabase.get(getApplication()).usuarioDao().findById(userId)));
    }

    public void consume() {
        state.setValue(State.IDLE);
    }

    public void saveProfile(String nome, String email, String curso, int semestre, String status) {
        if (state.getValue() == State.LOADING) return;
        state.setValue(State.LOADING);
        AppExecutors.io().execute(() -> {
            try {
                UserRepository.UpdateResult r = repository.updateProfile(userId, nome, email, curso, semestre, status);
                state.postValue(r == UserRepository.UpdateResult.OK ? State.SAVED
                        : r == UserRepository.UpdateResult.EMAIL_TAKEN ? State.EMAIL_TAKEN : State.ERROR);
            } catch (RuntimeException e) {
                state.postValue(State.ERROR);
            }
        });
    }

    public void changePassword(char[] atual, char[] nova) {
        if (state.getValue() == State.LOADING) {
            PasswordHasher.wipe(atual);
            PasswordHasher.wipe(nova);
            return;
        }
        state.setValue(State.LOADING);
        AppExecutors.io().execute(() -> {
            try {
                UserRepository.PasswordResult r = repository.changePassword(userId, atual, nova);
                state.postValue(r == UserRepository.PasswordResult.OK ? State.SAVED : State.WRONG_CURRENT);
            } catch (RuntimeException e) {
                state.postValue(State.ERROR);
            }
        });
    }
}
