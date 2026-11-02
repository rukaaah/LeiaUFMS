package br.ufms.leiaufms.core.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Executores do app: disco e banco nunca rodam na main thread (RNF-03). */
public final class AppExecutors {

    private static final ExecutorService IO = Executors.newFixedThreadPool(3);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private AppExecutors() {
    }

    public static ExecutorService io() {
        return IO;
    }

    public static void main(Runnable runnable) {
        MAIN.post(runnable);
    }
}
