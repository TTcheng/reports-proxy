package me.wcc.config;

import io.undertow.UndertowOptions;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.embedded.UndertowWebServerFactoryCustomizer;
import org.springframework.boot.web.embedded.undertow.ConfigurableUndertowWebServerFactory;
import org.springframework.boot.web.embedded.undertow.UndertowBuilderCustomizer;
import org.springframework.core.env.Environment;

/**
 * 嵌入的PowerBI发出的请求URL可能包含应该转义字符“{}”,web容器现在都默认不支持，并且报表由iframe的方式嵌入，发出的请求不受控制。
 * 只能通过修改Undertow的默认配置ALLOW_UNESCAPED_CHARACTERS_IN_URL，这个配置在Springboot自动配置中未暴露出来，因此在这里扩展。
 *
 * @author chuncheng.wang@hand-china.com 19-7-15 上午11:34
 */
public class AdditionalUndertowWebServerFactoryCustomizer extends UndertowWebServerFactoryCustomizer {
    public AdditionalUndertowWebServerFactoryCustomizer(Environment environment, ServerProperties serverProperties) {
        super(environment, serverProperties);
    }

    @Override
    public void customize(ConfigurableUndertowWebServerFactory factory) {
        super.customize(factory);
        UndertowBuilderCustomizer customizer = builder ->
                builder.setServerOption(UndertowOptions.ALLOW_UNESCAPED_CHARACTERS_IN_URL, true);
        factory.addBuilderCustomizers(customizer);
    }
}
