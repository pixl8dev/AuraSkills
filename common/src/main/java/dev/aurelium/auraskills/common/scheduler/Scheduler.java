package dev.aurelium.auraskills.common.scheduler;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dev.aurelium.auraskills.common.AuraSkillsPlugin;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class Scheduler {

    private static final int ASYNC_THREADS = Math.max(2, Math.min(8, Runtime.getRuntime().availableProcessors()));

    private final AuraSkillsPlugin plugin;

    private final ExecutorService asyncExecutor = createAsyncExecutor();
    private final ScheduledExecutorService asyncScheduler = Executors.newScheduledThreadPool(1,
            new ThreadFactoryBuilder().setNameFormat("auraskills-async-scheduler-%d").build());

    public Scheduler(final AuraSkillsPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract Task executeSync(final Runnable runnable);

    public Task executeAsync(final Runnable runnable) {
        return new SubmittedTask(asyncExecutor.submit(runnable));
    }

    public abstract Task scheduleSync(final Runnable runnable, final long delay, final TimeUnit timeUnit);

    public Task scheduleAsync(final Runnable runnable, final long delay, final TimeUnit timeUnit) {
        return new ScheduledTask(asyncScheduler.schedule(runnable, delay, timeUnit));
    }

    public abstract Task timerSync(final TaskRunnable runnable, final long delay, final long period, final TimeUnit timeUnit);

    public abstract Task timerAsync(final TaskRunnable runnable, final long delay, final long period, final TimeUnit timeUnit);

    // Should be run by the implementation when server is shutdown
    public void shutdown() {
        asyncExecutor.shutdown();
        asyncScheduler.shutdown();

        try {
            if (!asyncExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
            if (!asyncScheduler.awaitTermination(3, TimeUnit.SECONDS)) {
                asyncScheduler.shutdownNow();
            }
        } catch (final InterruptedException e) {
            asyncExecutor.shutdownNow();
            asyncScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private ExecutorService createAsyncExecutor() {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        return new ThreadPoolExecutor(
                ASYNC_THREADS,
                ASYNC_THREADS,
                0L,
                TimeUnit.MILLISECONDS,
                queue,
                new ThreadFactoryBuilder().setNameFormat("auraskills-async-task-%d").build()
        );
    }

}
