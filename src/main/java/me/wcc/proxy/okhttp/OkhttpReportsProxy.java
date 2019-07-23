package me.wcc.proxy.okhttp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import me.wcc.proxy.BaseProxyClient;
import me.wcc.proxy.okhttp.cookie.ReportsCookieManager;
import okhttp3.*;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * @author chuncheng.wang@hand-china.com 19-7-19 下午5:34
 */
@Slf4j
public class OkhttpReportsProxy extends BaseProxyClient {
    private final String domain;
    private final RestTemplate restTemplate;

    public OkhttpReportsProxy(String domain, Authenticator authenticator) {
        final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .authenticator(authenticator)
                .cookieJar(new ReportsCookieManager())
                .eventListener(new ReportsEventListener());
        ClientHttpRequestFactory requestFactory = new OkHttp3ClientHttpRequestFactory(clientBuilder.build());
        this.domain = domain;
        restTemplate = new RestTemplate(requestFactory);
    }

    @Override
    public ResponseEntity<byte[]> proxyGet(HttpServletRequest request, HttpServletResponse response, String uri) {
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, request.getHeader(HttpHeaders.ORIGIN));
        return restTemplate.getForEntity(getUrl(request, uri), byte[].class);
    }

    @Override
    public String proxyGet(String url) {
        return restTemplate.getForObject(url, String.class);
    }

    @Override
    public ResponseEntity<byte[]> proxy(HttpServletRequest request, HttpServletResponse response, String body, String uri) {
        HttpEntity<String> entity = new HttpEntity<>(body, getRequestHeaders(request));
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, request.getHeader(HttpHeaders.ORIGIN));
        return restTemplate.exchange(getUrl(request, uri), HttpMethod.valueOf(request.getMethod()), entity, byte[].class);
    }

    @Override
    public String getDomain() {
        return domain;
    }

    private String getUrl(HttpServletRequest request, String uri) {
        return getDomain() + Optional.ofNullable(uri)
                .orElse(Optional.ofNullable(request.getQueryString())
                        .map(s -> request.getRequestURI() + '?' + s).orElse(request.getRequestURI()));
    }

    private MultiValueMap<String, String> getRequestHeaders(HttpServletRequest request) {
        Enumeration<String> names = request.getHeaderNames();
        MultiValueMap<String, String> headers = new HttpHeaders();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (ifRemoveHeader(name)) {
                continue;
            }
            headers.add(name, request.getHeader(name));
        }
        return headers;
    }
}
