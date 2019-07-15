package me.wcc.ntlm.config;

import io.undertow.Undertow;
import me.wcc.ntlm.proxy.BasicReportsProxy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author chuncheng.wang@hand-china.com 19-7-13 下午1:18
 */
@Configuration
@EnableConfigurationProperties(ReportsProperties.class)
public class ReportsConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "reports", value = {"username", "password", "domain"})
    BasicReportsProxy basicAuthHttpClient(@Value("${reports.username}") String username,
                                          @Value("${reports.password}") String password,
                                          @Value("${reports.domain}") String domain) {
        return new BasicReportsProxy(username, password, domain);
    }

    @Bean
    @ConditionalOnClass(Undertow.class)
    public WebServerFactoryCustomizer webServerFactoryCustomizer(Environment environment, ServerProperties serverProperties) {
        return new AdditionalUndertowWebServerFactoryCustomizer(environment, serverProperties);
    }
}
