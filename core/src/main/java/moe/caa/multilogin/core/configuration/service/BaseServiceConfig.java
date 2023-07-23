package moe.caa.multilogin.core.configuration.service;

import lombok.Getter;
import moe.caa.multilogin.core.configuration.ConfException;
import moe.caa.multilogin.core.configuration.MojangApiConfig;
import moe.caa.multilogin.core.configuration.SkinRestorerConfig;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.BiFunction;

@Getter
public abstract class BaseServiceConfig {
    private final int id;
    private final String name;
    private final InitUUID initUUID;
    private final boolean whitelist;
    private final SkinRestorerConfig skinRestorer;
    private final MojangApiConfig mojangApi;

    protected BaseServiceConfig(int id, String name, InitUUID initUUID,
                                boolean whitelist, SkinRestorerConfig skinRestorer, MojangApiConfig mojangApi) throws ConfException {
        this.id = id;
        this.name = name;
        this.initUUID = initUUID;
        this.whitelist = whitelist;
        this.skinRestorer = skinRestorer;
        this.mojangApi = mojangApi;

        checkValid();
    }

    protected void checkValid() throws ConfException {
        if (this.id > 127 || this.id < 0)
            throw new ConfException(String.format(
                    "Yggdrasil id %d is out of bounds, The value can only be between 0 and 127."
                    , this.id
            ));
    }


    public abstract ServiceType getServiceType();

    /**
     * 初始化的UUID生成器
     */
    public enum InitUUID {
        DEFAULT((u, n) -> u),
        OFFLINE((u, n) -> UUID.nameUUIDFromBytes(("OfflinePlayer:" + n).getBytes(StandardCharsets.UTF_8))),
        RANDOM((u, n) -> UUID.randomUUID());

        private final BiFunction<UUID, String, UUID> biFunction;

        InitUUID(BiFunction<UUID, String, UUID> biFunction) {
            this.biFunction = biFunction;
        }

        public UUID generateUUID(UUID onlineUUID, String currentUsername) {
            return biFunction.apply(onlineUUID, currentUsername);
        }
    }
}
