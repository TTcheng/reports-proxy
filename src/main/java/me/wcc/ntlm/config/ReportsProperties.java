package me.wcc.ntlm.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author chuncheng.wang@hand-china.com 19-7-13 下午1:18
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "reports")
public class ReportsProperties {
    private String username;
    private String password;
    private String domain;
}
