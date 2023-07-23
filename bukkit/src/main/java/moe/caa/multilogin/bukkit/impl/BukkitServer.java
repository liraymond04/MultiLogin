package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.api.plugin.BaseScheduler;
import moe.caa.multilogin.api.plugin.IPlayerManager;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.api.plugin.IServer;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BukkitServer implements IServer {
    private final MultiLoginBukkit multiLoginBukkit;
    private final BaseScheduler bkScheduler;
    private final BukkitPlayerManager playerManager;
    private final Set<Pair<UUID, String>> whitelist;

    public BukkitServer(MultiLoginBukkit multiLoginBukkit) {
        this.multiLoginBukkit = multiLoginBukkit;
        bkScheduler = new BaseScheduler() {
            @Override
            public void runTask(Runnable run, long delay) {
                multiLoginBukkit.getServer().getScheduler().runTaskLater(multiLoginBukkit, run, delay);
            }
        };
        playerManager = new BukkitPlayerManager(multiLoginBukkit.getServer());
        whitelist = new HashSet<>();
        for (OfflinePlayer player : multiLoginBukkit.getServer().getWhitelistedPlayers()) {
            whitelist.add(new Pair<>(player.getUniqueId(), player.getName()));
        }
    }

    @Override
    public BaseScheduler getScheduler() {
        return bkScheduler;
    }

    @Override
    public IPlayerManager getPlayerManager() {
        return playerManager;
    }

    @Override
    public Set<Pair<UUID, String>> getWhitelist() {
        return whitelist;
    }

    @Override
    public boolean isOnlineMode() {
        return multiLoginBukkit.getServer().getOnlineMode();
    }

    @Override
    public boolean isForwarded() {
        return true;
    }

    @Override
    public String getName() {
        return multiLoginBukkit.getServer().getName();
    }

    @Override
    public String getVersion() {
        return multiLoginBukkit.getServer().getVersion();
    }

    @Override
    public void shutdown() {
        multiLoginBukkit.getServer().shutdown();
    }

    @Override
    public ISender getConsoleSender() {
        return new BukkitSender(multiLoginBukkit.getServer().getConsoleSender());
    }

    @Override
    public boolean pluginHasEnabled(String id) {
        return multiLoginBukkit.getServer().getPluginManager().getPlugin(id) != null;
    }
}
