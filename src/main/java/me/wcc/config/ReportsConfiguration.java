package me.wcc.config;

import io.undertow.Undertow;
import me.wcc.proxy.ProxyClient;
import me.wcc.proxy.okhttp.OkhttpReportsProxy;
import me.wcc.proxy.okhttp.auth.BasicAuthenticator;
import me.wcc.proxy.okhttp.auth.ntlm.NTLMAuthenticator;
import okhttp3.Authenticator;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author chuncheng.wang@hand-china.com 19-7-13 下午1:18
 */
@Configuration
@EnableConfigurationProperties(ReportsProperties.class)
public class ReportsConfiguration {

    @Bean
    @Primary
    @ConditionalOnProperty(
            prefix = ReportsProperties.PREFIX,
            value = {"proxyClient"},
            matchIfMissing = true,
            havingValue = "okhttp"
    )
    public ProxyClient okHttpProxyClient(ReportsProperties properties) {
        Authenticator authenticator;
        if (ReportsProperties.AUTH_NTLM.equalsIgnoreCase(properties.getAuthType())) {
            authenticator = new NTLMAuthenticator(properties.getUsername(), properties.getPassword(),
                    properties.getDomain(), "");
        } else {
            authenticator = new BasicAuthenticator(properties.getUsername(), properties.getPassword());
        }
        return new OkhttpReportsProxy(properties.getDomain(), authenticator);
    }

    //@Bean
    //@ConditionalOnMissingBean
    //public RestTemplate apacheRestTemplate(ReportsProperties properties) {
    //    Credentials credentials;
    //    if (ReportsProperties.AUTH_NTLM.equalsIgnoreCase(properties.getAuthType())) {
    //        credentials = new NTCredentials(properties.getUsername(), properties.getPassword(), "",
    //                properties.getDomain());
    //    } else {
    //        credentials = new UsernamePasswordCredentials(properties.getUsername(), properties.getPassword());
    //    }
    //    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    //    credentialsProvider.setCredentials(AuthScope.ANY, credentials);
    //    CloseableHttpClient httpClient = HttpClientBuilder.create()
    //            .setDefaultCredentialsProvider(credentialsProvider).build();
    //    ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
    //    return new RestTemplate(requestFactory);
    //}

    @Bean
    @Primary
    @ConditionalOnClass(Undertow.class)
    public WebServerFactoryCustomizer webServerFactoryCustomizer(Environment environment, ServerProperties serverProperties) {
        return new AdditionalUndertowWebServerFactoryCustomizer(environment, serverProperties);
    }
}
