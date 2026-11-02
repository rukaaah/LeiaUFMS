package br.ufms.leiaufms.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import br.ufms.leiaufms.core.security.PasswordHasher;
import br.ufms.leiaufms.core.util.AppExecutors;
import br.ufms.leiaufms.data.repository.AuthRepository;

/** Estado de login e cadastro (RF-02, RF-03). O trabalho de BCrypt e Room roda fora da main thread. */
public class AuthViewModel extends AndroidViewModel {

    public enum State { IDLE, LOADING, SUCCESS, INVALID_CREDENTIALS, EMAIL_TAKEN, ERROR }

    private final AuthRepository repository;
    private final MutableLiveData<State> state = new MutableLiveData<>(State.IDLE);

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application);
    }

    public LiveData<State> state() {
        return state;
    }

    public void consume() {
        state.setValue(State.IDLE);
    }

    public void login(String email, char[] senha) {
        if (state.getValue() == State.LOADING) {
            PasswordHasher.wipe(senha);
            return;
        }
        state.setValue(State.LOADING);
        AppExecutors.io().execute(() -> {
            try {
                state.postValue(repository.login(email, senha) ? State.SUCCESS : State.INVALID_CREDENTIALS);
            } catch (RuntimeException e) {
                state.postValue(State.ERROR);
            }
        });
    }

    public void register(String nome, String email, String curso, int semestre, char[] senha) {
        if (state.getValue() == State.LOADING) {
            PasswordHasher.wipe(senha);
            return;
        }
        state.setValue(State.LOADING);
        AppExecutors.io().execute(() -> {
            try {
                AuthRepository.RegisterResult r = repository.register(nome, email, curso, semestre, senha);
                state.postValue(r == AuthRepository.RegisterResult.OK ? State.SUCCESS : State.EMAIL_TAKEN);
            } catch (RuntimeException e) {
                state.postValue(State.ERROR);
            }
        });
    }
}
