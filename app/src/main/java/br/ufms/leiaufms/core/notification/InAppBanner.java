package br.ufms.leiaufms.core.notification;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Aviso do próprio app, no nosso tema, quando a pessoa está com o app aberto (feedback 03/11).
 * Com o app em segundo plano, o aviso continua indo para a notificação do sistema.
 */
public final class InAppBanner {

    /** Um aviso; {@code shown} evita reexibir quando a tela é recriada. */
    public static final class Event {
        public final long id;
        public final String canal;
        public final String titulo;
        public final String texto;
        public final String destinoTipo;
        public final Long destinoId;
        public boolean shown;

        Event(long id, String canal, String titulo, String texto, String destinoTipo, Long destinoId) {
            this.id = id;
            this.canal = canal;
            this.titulo = titulo;
            this.texto = texto;
            this.destinoTipo = destinoTipo;
            this.destinoId = destinoId;
        }
    }

    private static final MutableLiveData<Event> EVENTS = new MutableLiveData<>();
    private static int started;

    private InAppBanner() {
    }

    public static LiveData<Event> events() {
        return EVENTS;
    }

    public static boolean isForeground() {
        return started > 0;
    }

    static void post(long id, String canal, String titulo, String texto, String destinoTipo, Long destinoId) {
        EVENTS.postValue(new Event(id, canal, titulo, texto, destinoTipo, destinoId));
    }

    /** Conta as telas visíveis para saber se o app está em primeiro plano. */
    public static void track(Application app) {
        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityStarted(@NonNull Activity a) {
                started++;
            }

            @Override
            public void onActivityStopped(@NonNull Activity a) {
                started = Math.max(0, started - 1);
            }

            @Override
            public void onActivityCreated(@NonNull Activity a, @Nullable Bundle b) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity a) {
            }

            @Override
            public void onActivityPaused(@NonNull Activity a) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity a, @NonNull Bundle b) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity a) {
            }
        });
    }
}
