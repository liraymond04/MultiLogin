package moe.caa.multilogin.core.configuration.service.yggdrasil;

import moe.caa.multilogin.core.configuration.ConfException;
import moe.caa.multilogin.core.configuration.MojangApiConfig;
import moe.caa.multilogin.core.configuration.ProxyConfig;
import moe.caa.multilogin.core.configuration.SkinRestorerConfig;
import moe.caa.multilogin.core.configuration.service.ServiceType;

/**
 * Blessing Skin 皮肤站 Yggdrasil
 */
public class BlessingSkinYggdrasilServiceConfig extends BaseYggdrasilServiceConfig {
    private final String apiRoot;

    public BlessingSkinYggdrasilServiceConfig(int id, String name, InitUUID initUUID, boolean whitelist, SkinRestorerConfig skinRestorer, MojangApiConfig mojangApi, boolean trackIp, int timeout, int retry, long retryDelay, ProxyConfig authProxy, String apiRoot) throws ConfException {
        super(id, name, initUUID, whitelist, skinRestorer, mojangApi, trackIp, timeout, retry, retryDelay, authProxy);
        if (!apiRoot.endsWith("/")) {
            apiRoot = apiRoot.concat("/");
        }
        this.apiRoot = apiRoot;
    }


    @Override
    protected String getAuthURL() {
        return apiRoot.concat("session")
                .concat("server")
                .concat("/session")
                .concat("/minecraft")
                .concat("/hasJoined?")
                .concat("username={0}&serverId={1}{2}");
    }

    @Override
    protected String getAuthPostContent() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String getAuthTrackIpContent() {
        return "&ip={0}";
    }

    @Override
    public HttpRequestMethod getHttpRequestMethod() {
        return HttpRequestMethod.GET;
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.BLESSING_SKIN;
    }
}
