package io.reallmerry.rstudio.loader.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CountDownLatch;

public final class Platform {

    private Platform() {}

    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void scheduleOnMain(JavaPlugin plugin, Runnable task) {
        var latch = new CountDownLatch(1);
        if (isFolia()) {
            Bukkit.getGlobalRegionScheduler().run(plugin, t -> {
                try { task.run(); } finally { latch.countDown(); }
            });
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> {
                try { task.run(); } finally { latch.countDown(); }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void scheduleDelayedOnMain(JavaPlugin plugin, Runnable task, long ticks) {
        if (isFolia()) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, t -> task.run(), ticks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, ticks);
        }
    }

    public static void scheduleAsync(JavaPlugin plugin, Runnable task) {
        if (isFolia()) {
            Bukkit.getAsyncScheduler().runNow(plugin, t -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }
}