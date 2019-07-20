# Reports proxy

代理前台数据并添加验证信息，可选择使用httpclient或okhttp与报表服务器通信。

Note：使用前需要先配置自定义安全性。默认的Ntlm认证，理论上也是可行的，但是完全没有测试过。

配置认证方式和请求客户端
```properties
# 认证方式，ntlm/basic
reports.authType=ntlm
# 代理请求客户端，httpClient/okhttp
reports.proxyClient=okhttp
```