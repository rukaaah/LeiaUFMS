package br.ufms.leiaufms.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

import br.ufms.leiaufms.data.repository.TradeRepository;

/**
 * RF-24 e RF-33: expira pedidos sem resposta (72 h), cancela entregas vencidas (7 dias) e envia
 * lembretes. Roda a cada 6 horas com bateria não baixa (economia de energia) e também ao abrir o app.
 */
public class DeadlineWorker extends Worker {

    private static final String NAME = "prazos_dos_pedidos";

    public DeadlineWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            new TradeRepository(getApplicationContext()).runDeadlines(System.currentTimeMillis());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.retry();
        }
    }

    public static void schedule(Context context) {
        Constraints constraints = new Constraints.Builder().setRequiresBatteryNotLow(true).build();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(DeadlineWorker.class, 6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.KEEP, request);
    }
}
