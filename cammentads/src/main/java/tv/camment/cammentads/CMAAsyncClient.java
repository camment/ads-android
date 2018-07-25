/*
 * Created by Camment OY on 07/19/2018.
 * Copyright (c) 2018 Camment OY. All rights reserved.
 */

package tv.camment.cammentads;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * An async client executes actions in the background and returns the result on the UI thread.
 */
abstract class CMAAsyncClient {

    static final String CAMMENT_API = "https://api.camment.tv";

    private static final Handler UI_HANDLER = new Handler(Looper.getMainLooper());
    private static final Thread UI_THREAD = Looper.getMainLooper().getThread();

    private final ExecutorService executorService;

    CMAAsyncClient(ExecutorService executorService) {
        this.executorService = executorService;
    }

    <T> Future<T> submitTask(@NonNull final Callable<T> callable, @Nullable final CMACallback<T> callback) {
        Callable<T> call = new Callable<T>() {
            @Override
            public T call() throws Exception {
                try {
                    T result = callable.call();
                    onResult(result, callback);
                    return result;

                } catch (Exception e) {
                    onException(e, callback);
                    return null;
                }
            }
        };
        return executorService.submit(call);
    }

    private <T> void onResult(final T result, final CMACallback<T> callback) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(result);
            }
        });
    }

    private <T> void onException(final Exception e, final CMACallback<T> callback) {
        if (callback != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    callback.onException(e);
                }
            });
        }
    }

    private void runOnUiThread(@NonNull Runnable runnable) {
        if (Thread.currentThread() != UI_THREAD) {
            UI_HANDLER.post(runnable);
        } else {
            runnable.run();
        }
    }

}
