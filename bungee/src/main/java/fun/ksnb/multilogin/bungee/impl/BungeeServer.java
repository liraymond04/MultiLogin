package fun.ksnb.multilogin.bungee.impl;

import moe.caa.multilogin.api.plugin.BaseScheduler;
import moe.caa.multilogin.api.plugin.IPlayerManager;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.api.plugin.IServer;
import moe.caa.multilogin.api.util.Pair;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.conf.Configuration;

import java.util.Set;
import java.util.UUID;

/**
 * 代表Bungee 服务器
 */
public class BungeeServer implements IServer {
    private final ProxyServer bungeeCord;
    private final BaseScheduler scheduler;
    private final IPlayerManager playerManager;

    public BungeeServer(ProxyServer bungeeCord) {
        this.bungeeCord = bungeeCord;
        this.scheduler = new BungeeScheduler();
        this.playerManager = new BungeePlayerManager(bungeeCord);
    }

    @Override
    public BaseScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public IPlayerManager getPlayerManager() {
        return playerManager;
    }

    // TODO
    @Override
    public Set<Pair<UUID, String>> getWhitelist() {
        return null;
    }

    @Override
    public boolean isOnlineMode() {
        return bungeeCord.getConfig().isOnlineMode();
    }

    @Override
    public boolean isForwarded() {
        return ((Configuration) bungeeCord.getConfig()).isIpForward();
    }

    @Override
    public String getName() {
        return bungeeCord.getName();
    }

    @Override
    public String getVersion() {
        return bungeeCord.getVersion();
    }

    @Override
    public void shutdown() {
        bungeeCord.stop();
    }

    @Override
    public ISender getConsoleSender() {
        return new BungeeSender(bungeeCord.getConsole());
    }

    @Override
    public boolean pluginHasEnabled(String id) {
        for (Plugin plugin : bungeeCord.getPluginManager().getPlugins()) {
            if (plugin.getDescription().getName().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }
}
