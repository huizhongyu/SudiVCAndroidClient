package cn.closeli.rtc.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Administrator
 */
public final class ExecutorFactory {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    private static final Executor DEFAULT_WORK_EXECUTOR = new ThreadPoolExecutor(2 * CPU_COUNT + 1,
            2 * CPU_COUNT + 1,
            30, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(128),
            new ThreadFactory() {
                private final AtomicInteger mCount = new AtomicInteger(1);

                @Override
                public Thread newThread(@NonNull Runnable r) {
                    return new Thread(r, "ExecutorFactory - " + mCount.getAndIncrement());
                }
            }
    );

    private static final Executor DEFAULT_MAIN_EXECUTOR = new Executor() {
        private final Handler mHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mHandler.post(command);
        }
    };

    public static Executor getDefaultWorkExecutor() {
        return DEFAULT_WORK_EXECUTOR;
    }

    public static Executor getDefaultMainExecutor() {
        return DEFAULT_MAIN_EXECUTOR;
    }
}
