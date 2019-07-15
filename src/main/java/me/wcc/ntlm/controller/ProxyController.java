package me.wcc.ntlm.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import me.wcc.ntlm.proxy.BasicReportsProxy;
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
    private final BasicReportsProxy basicClient;

    public ProxyController(BasicReportsProxy basicClient) {
        this.basicClient = basicClient;
    }

    @GetMapping(value = {"/reports/**", "/powerbi/**"})
    public ResponseEntity<byte[]> getProxy(HttpServletRequest request, HttpServletResponse response) {
        return basicClient.proxy(request, response, "");
    }

    @RequestMapping(value = {"/reports/**", "/powerbi/**"})
    public ResponseEntity<byte[]> proxy(HttpServletRequest request, HttpServletResponse response,
                                        @RequestBody String body) {
        return basicClient.proxy(request, response, body);
    }
}
