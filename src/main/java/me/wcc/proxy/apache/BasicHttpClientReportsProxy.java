package me.wcc.proxy.apache;

import java.util.Base64;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHeader;
import org.springframework.http.HttpHeaders;

/**
 * @author chuncheng.wang@hand-china.com
 */
@Slf4j

public class BasicHttpClientReportsProxy extends ApacheProxyClient {
    private String username;
    private String password;
    private String domain;

    public BasicHttpClientReportsProxy(String username, String password, String domain) {
        this.username = username;
        this.password = password;
        this.domain = domain;
        init();
    }

    @Override
    Credentials getCredentials() {
        return new UsernamePasswordCredentials(username, password);
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    void beforeExecute(HttpUriRequest httpRequest) {
        String authPair = username + ':' + password;
        byte[] encode = Base64.getEncoder().encode(authPair.getBytes());
        final String authorization = "Basic " + new String(encode);
        log.debug("with AUTHORIZATION:{}", authorization);
        Header header = new BasicHeader(HttpHeaders.AUTHORIZATION, authorization);
        httpRequest.setHeader(header);
    }
}