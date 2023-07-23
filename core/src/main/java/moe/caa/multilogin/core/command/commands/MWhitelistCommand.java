package moe.caa.multilogin.core.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.plugin.ISender;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.Permissions;
import moe.caa.multilogin.core.command.argument.OnlineArgumentType;
import moe.caa.multilogin.core.command.argument.ServiceIdArgumentType;
import moe.caa.multilogin.core.command.argument.StringArgumentType;
import moe.caa.multilogin.core.configuration.service.BaseServiceConfig;
import moe.caa.multilogin.core.database.table.UserDataTableV3;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * /MultiLogin whitelist * 指令处理程序
 */
public class MWhitelistCommand {

    private final CommandHandler handler;

    public MWhitelistCommand(CommandHandler handler) {
        this.handler = handler;
    }

    public LiteralArgumentBuilder<ISender> register(LiteralArgumentBuilder<ISender> literalArgumentBuilder) {
        return literalArgumentBuilder
                .then(handler.literal("add")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_WHITELIST_ADD))
                        .then(handler.argument("username", StringArgumentType.string())
                                .executes(this::executeAddUsername)
                        )
                )
                .then(handler.literal("remove")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_WHITELIST_REMOVE))
                        .then(handler.argument("username", StringArgumentType.string())
                                .executes(this::executeRemoveUsername)
                        )
                ).then(handler.literal("specific")
                        .then(handler.literal("add")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_WHITELIST_SPECIFIC_ADD))
                                .then(handler.argument("online", OnlineArgumentType.online())
                                        .executes(this::executeAdd)

                                )
                        )
                        .then(handler.literal("remove")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_WHITELIST_SPECIFIC_REMOVE))
                                .then(handler.argument("online", OnlineArgumentType.online())
                                        .executes(this::executeRemove)
                                )

                        )
                        .then(handler.literal("list")
                                .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_WHITELIST_SPECIFIC_LIST))
                                .then(handler.argument("serviceid", ServiceIdArgumentType.service())
                                        .executes(this::executeList)
                                )
                        )
                ).then(handler.literal("copy")
                        .requires(sender -> sender.hasPermission(Permissions.COMMAND_MULTI_LOGIN_WHITELIST_COPY))
                        .then(handler.argument("serviceid", ServiceIdArgumentType.service())
                                .executes((this::executeCopy))
                        )
                );
    }

    // /MultiLogin whitelist permanent remove <serviceid> <onlineuuid>
    @SneakyThrows
    private int executeRemove(CommandContext<ISender> context) {
        OnlineArgumentType.OnlineArgument online = OnlineArgumentType.getOnline(context, "online");
        if (!online.isWhitelist()) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_permanent_remove_repeat",
                    new Pair<>("online_uuid", online.getOnlineUUID()),
                    new Pair<>("online_name", online.getOnlineName()),
                    new Pair<>("service_name", online.getBaseServiceConfig().getName()),
                    new Pair<>("service_id", online.getBaseServiceConfig().getId())
            ));
            return 0;
        }
        // 如果有白名单的话，表示有数据，直接更新不需要额外判断
        CommandHandler.getCore().getSqlManager().getUserDataTable().setWhitelist(online.getOnlineUUID(), online.getBaseServiceConfig().getId(), false);
        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_permanent_remove",
                new Pair<>("online_uuid", online.getOnlineUUID()),
                new Pair<>("online_name", online.getOnlineName()),
                new Pair<>("service_name", online.getBaseServiceConfig().getName()),
                new Pair<>("service_id", online.getBaseServiceConfig().getId())
        ));
        UUID inGameUUID = CommandHandler.getCore().getSqlManager().getUserDataTable().getInGameUUID(online.getOnlineUUID(), online.getBaseServiceConfig().getId());
        if (inGameUUID != null) {
            CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().kickPlayerIfOnline(inGameUUID, CommandHandler.getCore().getLanguageHandler().getMessage("in_game_whitelist_removed"));
        }
        return 0;
    }

    // /MultiLogin whitelist permanent add <serviceid> <onlineuuid>
    @SneakyThrows
    private int executeAdd(CommandContext<ISender> context) {
        OnlineArgumentType.OnlineArgument online = OnlineArgumentType.getOnline(context, "online");
        if (online.isWhitelist()) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_permanent_add_repeat",
                    new Pair<>("online_uuid", online.getOnlineUUID()),
                    new Pair<>("online_name", online.getOnlineName()),
                    new Pair<>("service_name", online.getBaseServiceConfig().getName()),
                    new Pair<>("service_id", online.getBaseServiceConfig().getId())
            ));
            return 0;
        }
        if (!CommandHandler.getCore().getSqlManager().getUserDataTable().dataExists(online.getOnlineUUID(), online.getBaseServiceConfig().getId())) {
            CommandHandler.getCore().getSqlManager().getUserDataTable().insertNewData(online.getOnlineUUID(), online.getBaseServiceConfig().getId(), online.getOnlineName(), null);
        }
        CommandHandler.getCore().getSqlManager().getUserDataTable().setWhitelist(online.getOnlineUUID(), online.getBaseServiceConfig().getId(), true);
        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_permanent_add",
                new Pair<>("online_uuid", online.getOnlineUUID()),
                new Pair<>("online_name", online.getOnlineName()),
                new Pair<>("service_name", online.getBaseServiceConfig().getName()),
                new Pair<>("service_id", online.getBaseServiceConfig().getId())
        ));
        return 0;
    }

    // /MultiLogin whitelist remove <name>
    @SneakyThrows
    private int executeRemoveUsername(CommandContext<ISender> context) {
        String username = StringArgumentType.getString(context, "username");
        int count = 0;
        if (CommandHandler.getCore().getCacheWhitelistHandler().getCachedWhitelist().remove(username)) {
            count++;
        }
        UUID inGameUUID = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(username);
        if (inGameUUID != null) {
            if (CommandHandler.getCore().getSqlManager().getUserDataTable().hasWhitelist(inGameUUID)) {
                count++;
                CommandHandler.getCore().getSqlManager().getUserDataTable().setWhitelist(inGameUUID, false);
            }
        }
        if (count == 0) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_remove_repeat",
                    new Pair<>("name", username)
            ));
            return 0;
        }
        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_remove",
                new Pair<>("name", username),
                new Pair<>("count", count)
        ));
        if(inGameUUID != null){
            IPlayer player = CommandHandler.getCore().getPlugin().getRunServer().getPlayerManager().getPlayer(inGameUUID);
            if (player != null) {
                player.kickPlayer(CommandHandler.getCore().getLanguageHandler().getMessage("in_game_whitelist_removed"));
            }
        }
        return 0;
    }

    // /MultiLogin whitelist remove <add>
    @SneakyThrows
    private int executeAddUsername(CommandContext<ISender> context) {
        String username = StringArgumentType.getString(context, "username").toLowerCase(Locale.ROOT);
        boolean have = false;
        UUID inGameUUID = CommandHandler.getCore().getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(username);
        if (inGameUUID != null) {
            have = CommandHandler.getCore().getSqlManager().getUserDataTable().hasWhitelist(inGameUUID);
        }
        if (have) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_add_repeat",
                    new Pair<>("name", username)
            ));
            return 0;
        }
        if (!CommandHandler.getCore().getCacheWhitelistHandler().getCachedWhitelist().add(username)) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_add_repeat",
                    new Pair<>("name", username)
            ));
            return 0;
        }

        context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_add",
                new Pair<>("name", username)
        ));
        return 0;
    }

    // /Multilogin whitelist specific list <serviceid>
    @SneakyThrows
    private int executeList(CommandContext<ISender> context) {
        BaseServiceConfig serviceConfig = ServiceIdArgumentType.getService(context, "serviceid");

        Set<Pair<UUID, String>> players = CommandHandler.getCore().getSqlManager().getUserDataTable().getWhitelist(serviceConfig.getId());
        if (players.size() == 0) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_permanent_list_none",
                    new Pair<>("service_name", serviceConfig.getName()),
                    new Pair<>("service_id", serviceConfig.getId())
            ));
            return 0;
        }
        for (Pair<UUID, String> player : players) {
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_permanent_list",
                    new Pair<>("online_uuid", player.getValue1()),
                    new Pair<>("online_name", player.getValue2()),
                    new Pair<>("service_name", serviceConfig.getName()),
                    new Pair<>("service_id", serviceConfig.getId())
            ));
        }
        return 0;
    }

    // /Multilogin whitelist copy <serviceid>
    @SneakyThrows
    private int executeCopy(CommandContext<ISender> context) {
        BaseServiceConfig serviceConfig = ServiceIdArgumentType.getService(context, "serviceid");
        Set<Pair<UUID, String>> players = CommandHandler.getCore().getPlugin().getRunServer().getWhitelist();;

        UserDataTableV3 dataTable = CommandHandler.getCore().getSqlManager().getUserDataTable();

        for (Pair<UUID, String> player : players) {
            UUID uuid = player.getValue1();
            String name = player.getValue2();
            if (!dataTable.dataExists(uuid, serviceConfig.getId())) {
                dataTable.insertNewData(uuid, serviceConfig.getId(), name, null);
            }
            if (dataTable.hasWhitelist(uuid, serviceConfig.getId())) {
                context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_permanent_add_repeat",
                        new Pair<>("online_uuid", uuid),
                        new Pair<>("online_name", name),
                        new Pair<>("service_name", serviceConfig.getName()),
                        new Pair<>("service_id", serviceConfig.getId())
                ));
                continue;
            }
            CommandHandler.getCore().getSqlManager().getUserDataTable().setWhitelist(uuid, serviceConfig.getId(), true);
            context.getSource().sendMessagePL(CommandHandler.getCore().getLanguageHandler().getMessage("command_message_whitelist_permanent_add",
                    new Pair<>("online_uuid", uuid),
                    new Pair<>("online_name", name),
                    new Pair<>("service_name", serviceConfig.getName()),
                    new Pair<>("service_id", serviceConfig.getId())
            ));
        }
        return 0;
    }
}
