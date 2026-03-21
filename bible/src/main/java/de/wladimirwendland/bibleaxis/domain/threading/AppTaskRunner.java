package de.wladimirwendland.bibleaxis.domain.threading;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppTaskRunner {

    private final ExecutorService ioExecutor;
    private final Handler mainHandler;

    public AppTaskRunner() {
        this(Executors.newSingleThreadExecutor(), new Handler(Looper.getMainLooper()));
    }

    AppTaskRunner(@NonNull ExecutorService ioExecutor, @NonNull Handler mainHandler) {
        this.ioExecutor = ioExecutor;
        this.mainHandler = mainHandler;
    }

    public void runOnIo(@NonNull Runnable runnable) {
        ioExecutor.execute(runnable);
    }

    public void runOnMain(@NonNull Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
            return;
        }
        mainHandler.post(runnable);
    }
}
