package me.wcc.controller;

import javax.servlet.http.HttpServletResponse;

import me.wcc.proxy.ProxyClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chuncheng.wang@hand-china.com 19-7-22 下午3:14
 */
@RestController
public class AuthController {
    private final ProxyClient proxyClient;

    public AuthController(ProxyClient proxyClient) {
        this.proxyClient = proxyClient;
    }

    @GetMapping(value = {"/report-server/powerbi/login"})
    public String login(HttpServletResponse response) {
        // 将状态置为Unauthorized，认证器才能继续与服务器交互
        response.setStatus(401);
        return proxyClient.proxyGet("http://192.168.12.158/ReportServer/Logon.aspx");
    }
}
