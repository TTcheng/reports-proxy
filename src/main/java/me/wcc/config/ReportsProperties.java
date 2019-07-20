package me.wcc.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author chuncheng.wang@hand-china.com 19-7-13 下午1:18
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ConfigurationProperties(prefix = ReportsProperties.PREFIX)
public class ReportsProperties {
    public static final String PREFIX = "reports";

    public static final String CLIENT_APACHE = "httpClient";
    public static final String CLIENT_OKHTTP = "okhttp";

    public static final String AUTH_BASIC = "basic";
    public static final String AUTH_NTLM = "ntlm";

    private String username;
    private String password;
    private String domain;
    private String authType = AUTH_BASIC;
    private String proxyClient = CLIENT_OKHTTP;
}
