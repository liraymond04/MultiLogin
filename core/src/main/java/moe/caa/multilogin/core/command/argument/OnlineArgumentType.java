package moe.caa.multilogin.core.command.argument;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.api.util.There;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.command.UniversalCommandExceptionType;
import moe.caa.multilogin.core.configuration.service.BaseServiceConfig;
import moe.caa.multilogin.core.database.table.UserDataTableV3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Online 参数阅读程序
 * <service_id> <online_uuid|online_name>
 */
public class OnlineArgumentType implements ArgumentType<OnlineArgumentType.OnlineArgument> {

    public static OnlineArgumentType online() {
        return new OnlineArgumentType();
    }

    public static OnlineArgumentType.OnlineArgument getOnline(final CommandContext<?> context, final String name) {
        return context.getArgument(name, OnlineArgumentType.OnlineArgument.class);
    }

    @SneakyThrows
    @Override
    public OnlineArgument parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();
        BaseServiceConfig serviceConfig = ServiceIdArgumentType.readServiceConfig(reader);
        if (!reader.canRead()) {
            reader.setCursor(i);
            throw CommandHandler.getBuiltInExceptions().dispatcherUnknownCommand().createWithContext(reader);
        }
        reader.skip();
        String nameOrUuid = StringArgumentType.readString(reader);

        UUID uuid = ValueUtil.getUuidOrNull(nameOrUuid);

        // fetch UUID from Mojang API
        if (uuid == null && serviceConfig.getMojangApi().getApiUrl() != null && serviceConfig.getMojangApi().toString() != null) {
            try {
//                System.out.println("Start try... " + serviceConfig.getMojangApi().getUsernameToUUIDRequest(nameOrUuid));
                URL urlForGetRequest = new URL(serviceConfig.getMojangApi().getUsernameToUUIDRequest(nameOrUuid));
                String readLine;
                HttpURLConnection connection = (HttpURLConnection) urlForGetRequest.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("id", "name");
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    while ((readLine = in.readLine()) != null) {
                        response.append(readLine);
                    } in.close();

                    JsonObject _response = new Gson().fromJson(response.toString(), JsonObject.class);
                    
                    UUID _uuid = ValueUtil.getUuidOrNull(_response.get("id").getAsString());
                    String _name = _response.get("name").getAsString();

//                    System.out.println("Got uuid: " + _response.get("id").getAsString());
                    UserDataTableV3 dataTable = CommandHandler.getCore().getSqlManager().getUserDataTable();
//                    System.out.println("Has user data: " + dataTable.dataExists(_uuid, serviceConfig.getId()));
                    boolean whitelist = dataTable.hasWhitelist(_uuid, serviceConfig.getId());

                    return new OnlineArgument(serviceConfig, _uuid, _name, _uuid, whitelist);
                } else {
                    // HTTP response not good
                }
            } catch (MalformedURLException e) {
                // Incorrect/no Mojang API url provided
            } catch (Exception e) {
                // TODO - throw exception for fail to fetch UUID
//                System.out.println(e);
                throw CommandHandler.getBuiltInExceptions().dispatcherUnknownCommand().createWithContext(reader);
            }
        }

        // previous implementation (lookup saved UUIDs)
        UserDataTableV3 dataTable = CommandHandler.getCore().getSqlManager().getUserDataTable();

        if (uuid == null) {
            uuid = dataTable.getOnlineUUID(nameOrUuid, serviceConfig.getId());
            if (uuid == null) {
                reader.setCursor(i);
                throw UniversalCommandExceptionType.create(
                        CommandHandler.getCore().getLanguageHandler().getMessage("command_message_online_not_found_by_name",
                                new Pair<>("service_name", serviceConfig.getName()),
                                new Pair<>("service_id", serviceConfig.getId()),
                                new Pair<>("online_name", nameOrUuid)
                        ), reader);
            }
        }
        There<String, UUID, Boolean> there = dataTable.get(uuid, serviceConfig.getId());
        if (there == null) {
            reader.setCursor(i);
            throw UniversalCommandExceptionType.create(
                    CommandHandler.getCore().getLanguageHandler().getMessage("command_message_online_not_found_by_uuid",
                            new Pair<>("service_name", serviceConfig.getName()),
                            new Pair<>("service_id", serviceConfig.getId()),
                            new Pair<>("online_uuid", uuid)
                    ), reader);
        }
        return new OnlineArgument(serviceConfig, uuid, there.getValue1(), there.getValue2(), there.getValue3());
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return ServiceIdArgumentType.getSuggestions(context, builder);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Data
    public static class OnlineArgument {
        private final BaseServiceConfig baseServiceConfig;
        private final UUID onlineUUID;
        private final String onlineName;
        private final UUID profileUUID;
        private final boolean whitelist;
    }
}
