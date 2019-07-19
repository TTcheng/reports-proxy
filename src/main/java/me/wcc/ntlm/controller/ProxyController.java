package me.wcc.ntlm.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import me.wcc.ntlm.proxy.AbstractReportsProxy;
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
    private final AbstractReportsProxy reportsProxy;

    public ProxyController(AbstractReportsProxy reportsProxy) {
        this.reportsProxy = reportsProxy;
    }

    @GetMapping(value = {"/reports/**", "/powerbi/**"})
    public ResponseEntity<byte[]> getProxy(HttpServletRequest request, HttpServletResponse response) {
        return reportsProxy.proxy(request, response, "");
    }

    @RequestMapping(value = {"/reports/**", "/powerbi/**"})
    public ResponseEntity<byte[]> proxy(HttpServletRequest request, HttpServletResponse response,
                                        @RequestBody String body) {
        return reportsProxy.proxy(request, response, body);
    }
}
