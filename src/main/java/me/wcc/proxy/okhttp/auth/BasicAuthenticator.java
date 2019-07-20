package me.wcc.proxy.okhttp.auth;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpHeaders;

/**
 * @author chuncheng.wang@hand-china.com 19-7-20 下午2:55
 */
@Slf4j
public class BasicAuthenticator implements Authenticator {
    private final String username;
    private final String password;

    public BasicAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NotNull Response response) {
        if (response.request().header(HttpHeaders.AUTHORIZATION) != null) {
            // Give up, we've already attempted to authenticate.
            return null;
        }
        log.debug("Authenticating for response: {}", response);
        log.debug("Challenges: {}", response.challenges());
        String credential = Credentials.basic(username, password);

        return response.request().newBuilder().header(HttpHeaders.AUTHORIZATION, credential).build();
    }
}
