# Reports proxy

代理前台数据并添加验证信息，使用httpclient与报表服务器通信。后期可考虑换成okhttp

Note：使用前需要先配置自定义安全性。默认的Ntlm认证，理论上也是可行的，但是完全没有测试过。

要使用NTLM的认证方式，可以考虑使用以下代码替换ReportsConfiguration中的配置。
```java
    @Bean
    @ConditionalOnProperty(prefix = "reports", value = {"username", "password", "domain"})
    public AbstractReportsProxy reportsProxy(@Value("${reports.username}") String username,
                                             @Value("${reports.password}") String password,
                                             @Value("${reports.domain}") String domain) {
        return new NtlmReportsProxy(username, password, domain, "");
    }
```