package me.wcc.config;

import io.undertow.Undertow;
import me.wcc.proxy.ProxyClient;
import me.wcc.proxy.apache.BasicHttpClientReportsProxy;
import me.wcc.proxy.apache.NtlmHttpClientReportsProxy;
import me.wcc.proxy.okhttp.OkhttpReportsProxy;
import me.wcc.proxy.okhttp.auth.BasicAuthenticator;
import me.wcc.proxy.okhttp.auth.ntlm.NTLMAuthenticator;
import okhttp3.Authenticator;
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

    @Bean
    @ConditionalOnMissingBean
    public ProxyClient apacheProxyClient(ReportsProperties properties) {
        if (ReportsProperties.AUTH_NTLM.equalsIgnoreCase(properties.getAuthType())) {
            return new NtlmHttpClientReportsProxy(properties.getUsername(), properties.getPassword(), properties.getDomain(), "");
        }
        return new BasicHttpClientReportsProxy(properties.getUsername(), properties.getPassword(), properties.getDomain());
    }

    @Bean
    @Primary
    @ConditionalOnClass(Undertow.class)
    public WebServerFactoryCustomizer webServerFactoryCustomizer(Environment environment, ServerProperties serverProperties) {
        return new AdditionalUndertowWebServerFactoryCustomizer(environment, serverProperties);
    }
}
