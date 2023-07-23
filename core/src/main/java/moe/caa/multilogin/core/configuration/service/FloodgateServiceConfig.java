package moe.caa.multilogin.core.configuration.service;

import moe.caa.multilogin.core.configuration.ConfException;
import moe.caa.multilogin.core.configuration.MojangApiConfig;
import moe.caa.multilogin.core.configuration.SkinRestorerConfig;

public class FloodgateServiceConfig extends BaseServiceConfig {
    public FloodgateServiceConfig(int id, String name, InitUUID initUUID,
                                  boolean whitelist, SkinRestorerConfig skinRestorer, MojangApiConfig mojangApi) throws ConfException {
        super(id, name, initUUID, whitelist, skinRestorer, mojangApi);
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.FLOODGATE;
    }
}
