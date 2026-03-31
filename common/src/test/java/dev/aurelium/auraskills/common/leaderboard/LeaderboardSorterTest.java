package dev.aurelium.auraskills.common.leaderboard;

import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.message.PlatformLogger;
import dev.aurelium.auraskills.common.region.RegionManager;
import dev.aurelium.auraskills.common.scheduler.Scheduler;
import dev.aurelium.auraskills.common.scheduler.Task;
import dev.aurelium.auraskills.common.scheduler.TaskRunnable;
import dev.aurelium.auraskills.common.scheduler.TaskStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LeaderboardSorterTest {

    @Test
    void testCompare() {
        LeaderboardSorter sorter = new LeaderboardSorter();

        assertEquals(0, sorter.compare(new SkillValue(UUID.randomUUID(), 0, 0.0), new SkillValue(UUID.randomUUID(), 0, 0.0)));
        assertEquals(1, sorter.compare(new SkillValue(UUID.randomUUID(), 0, 0.0), new SkillValue(UUID.randomUUID(), 1, 0.0)));
        assertEquals(1, sorter.compare(new SkillValue(UUID.randomUUID(), 0, 5.0), new SkillValue(UUID.randomUUID(), 1, 0.0)));
        assertEquals(-1, sorter.compare(new SkillValue(UUID.randomUUID(), 1, 0.0), new SkillValue(UUID.randomUUID(), 0, 0.0)));
        assertEquals(-8000, sorter.compare(new SkillValue(UUID.randomUUID(), 1, 100.0), new SkillValue(UUID.randomUUID(), 1, 20.0)));
        assertEquals(100, sorter.compare(new SkillValue(UUID.randomUUID(), 1, 100000.0), new SkillValue(UUID.randomUUID(), 1, 100001.0)));
    }

    @Test
    void testRegionManagerUsesAsyncReset() {
        RecordingScheduler scheduler = new RecordingScheduler();
        RegionManager regionManager = new RegionManager(createPlugin(scheduler)) {
            @Override
            public boolean isChunkLoaded(String worldName, int chunkX, int chunkZ) {
                return false;
            }
        };

        regionManager.saveAllRegions(false, false);

        assertEquals(0, scheduler.syncSchedules);
        assertEquals(1, scheduler.asyncSchedules);
    }

    private AuraSkillsPlugin createPlugin(RecordingScheduler scheduler) {
        PlatformLogger logger = new PlatformLogger() {
            @Override
            public void info(String message) {
            }

            @Override
            public void warn(String message) {
            }

            @Override
            public void warn(String message, Throwable throwable) {
            }

            @Override
            public void severe(String message) {
            }

            @Override
            public void severe(String message, Throwable throwable) {
            }

            @Override
            public void debug(String message) {
            }
        };
        return (AuraSkillsPlugin) Proxy.newProxyInstance(
                AuraSkillsPlugin.class.getClassLoader(),
                new Class<?>[]{AuraSkillsPlugin.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getScheduler" -> scheduler;
                    case "logger" -> logger;
                    default -> null;
                });
    }

    private static final class RecordingScheduler extends Scheduler {

        private int syncSchedules;
        private int asyncSchedules;

        private RecordingScheduler() {
            super(null);
        }

        @Override
        public Task executeSync(Runnable runnable) {
            runnable.run();
            return NoopTask.INSTANCE;
        }

        @Override
        public Task scheduleSync(Runnable runnable, long delay, TimeUnit timeUnit) {
            syncSchedules++;
            runnable.run();
            return NoopTask.INSTANCE;
        }

        @Override
        public Task scheduleAsync(Runnable runnable, long delay, TimeUnit timeUnit) {
            asyncSchedules++;
            runnable.run();
            return NoopTask.INSTANCE;
        }

        @Override
        public Task timerSync(TaskRunnable runnable, long delay, long period, TimeUnit timeUnit) {
            return NoopTask.INSTANCE;
        }

        @Override
        public Task timerAsync(TaskRunnable runnable, long delay, long period, TimeUnit timeUnit) {
            return NoopTask.INSTANCE;
        }
    }

    private enum NoopTask implements Task {
        INSTANCE;

        @Override
        public TaskStatus getStatus() {
            return TaskStatus.STOPPED;
        }

        @Override
        public void cancel() {
        }
    }
}
