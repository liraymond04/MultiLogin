package moe.caa.multilogin.core.configuration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * 表示一个Mojang API配置
 */
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
@ToString
public class MojangApiConfig {
    private final String apiUrl;
    private final String usernameToUUIDPath;

    public static MojangApiConfig read(CommentedConfigurationNode node) throws SerializationException, ConfException {
        String apiUrl = node.node("apiUrl").getString("");
        String usernameToUUIDPath = node.node("usernameToUUIDPath").getString("");

        return new MojangApiConfig(apiUrl, usernameToUUIDPath);
    }

    public String getUsernameToUUIDRequest(String username) {
        return apiUrl + usernameToUUIDPath + username;
    }
}
