package me.wcc.proxy.apache;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.auth.*;
import org.apache.http.client.methods.HttpUriRequest;


/**
 * @author chuncheng.wang@hand-china.com
 */
@Slf4j
public class NtlmHttpClientReportsProxy extends BaseApacheProxyClient {
    private String username;
    private String password;
    private String domain;
    private String workstation;

    public NtlmHttpClientReportsProxy(String username, String password, String domain, String workstation) {
        this.username = username;
        this.password = password;
        this.domain = domain;
        this.workstation = workstation;
        init();
    }

    @Override
    Credentials getCredentials() {
        return new NTCredentials(username, password, workstation, domain);
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    void beforeExecute(HttpUriRequest httpRequest) {
        // do nothing
    }
}
