package br.ufms.leiaufms;

import android.app.Application;

import br.ufms.leiaufms.data.local.AppDatabase;

/** Application: inicializa o banco. Canais de notificação e WorkManager entram nas partes 2 e 3. */
public class LeiaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppDatabase.get(this);
    }
}
