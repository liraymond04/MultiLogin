package moe.caa.multilogin.api.plugin;

import moe.caa.multilogin.api.util.Pair;

import java.util.Set;
import java.util.UUID;

public interface IServer {

    /**
     * 获得线程调度器对象
     *
     * @return 线程调度器对象
     */
    BaseScheduler getScheduler();

    /**
     * 获得玩家管理器
     *
     * @return 获得玩家管理器
     */
    IPlayerManager getPlayerManager();

    /**
     * Get native whitelist
     *
     * @return set of players
     */
    Set<Pair<UUID, String>> getWhitelist();

    /**
     * 是否开启在线验证
     *
     * @return 在线验证模式
     */
    boolean isOnlineMode();

    /**
     * 返回是否已开启最基本的 UUID 穿透功能
     */
    boolean isForwarded();

    /**
     * 获得服务器核心名称
     *
     * @return 服务器核心名称
     */
    String getName();

    /**
     * 获得服务器版本
     *
     * @return 服务器版本
     */
    String getVersion();

    /**
     * 关闭服务器
     */
    void shutdown();

    /**
     * 获得控制台对象
     */
    ISender getConsoleSender();

    /**
     * 判断一个插件有没有被加载
     */
    boolean pluginHasEnabled(String id);
}
