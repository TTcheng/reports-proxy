package me.wcc.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import me.wcc.proxy.ProxyClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chuncheng.wang@hand-china.com
 */
@Slf4j
@RestController
public class ProxyController {
    private final ProxyClient proxyClient;

    public ProxyController(ProxyClient proxyClient) {
        this.proxyClient = proxyClient;
    }

    @GetMapping(value = {"/reports/**", "/powerbi/**"})
    public ResponseEntity<byte[]> getProxy(HttpServletRequest request, HttpServletResponse response) {
        return proxyClient.proxyGet(request, response);
    }

    @RequestMapping(value = {"/reports/**", "/powerbi/**"})
    public ResponseEntity<byte[]> proxy(HttpServletRequest request, HttpServletResponse response,
                                        @RequestBody String body) {
        return proxyClient.proxy(request, response, body);
    }
}
