package br.ufms.leiaufms;

import android.app.Application;

import br.ufms.leiaufms.core.notification.Notifier;
import br.ufms.leiaufms.data.local.AppDatabase;
import br.ufms.leiaufms.work.DeadlineWorker;

/** Application: inicializa o banco. cria os canais de notificação e agenda a verificação de prazos. */
public class LeiaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppDatabase.prepare(this);
        Notifier.createChannels(this);
        DeadlineWorker.schedule(this);
    }
}
