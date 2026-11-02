package br.ufms.leiaufms;

import android.app.Application;

import br.ufms.leiaufms.core.notification.Notifier;
import br.ufms.leiaufms.data.local.AppDatabase;

/** Application: inicializa o banco. cria os canais de notificação. WorkManager entra na Parte 3. */
public class LeiaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppDatabase.prepare(this);
        Notifier.createChannels(this);
    }
}
