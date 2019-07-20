package me.wcc.proxy.okhttp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import kotlin.Pair;
import lombok.extern.slf4j.Slf4j;
import me.wcc.proxy.BaseProxyClient;
import okhttp3.*;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * @author chuncheng.wang@hand-china.com 19-7-19 下午5:34
 */
@Slf4j
public class OkhttpReportsProxy extends BaseProxyClient {
    private final String domain;
    private final OkHttpClient client;

    public OkhttpReportsProxy(String domain, Authenticator authenticator) {
        this.domain = domain;
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .authenticator(authenticator)
                .eventListener(new ReportsEventListener())
                .build();
    }

    private Headers getRequestHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        Headers.Builder headers = new Headers.Builder();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (ifRemoveHeader(headerName)) {
                continue;
            }
            headers.add(headerName, request.getHeader(headerName));
        }
        return headers.build();
    }

    private Request buildRequest(HttpServletRequest servletRequest, String body, String uri) {
        // uri
        final String finalUri = Optional.ofNullable(uri)
                .orElse(Optional.ofNullable(servletRequest.getQueryString())
                        .map(s -> servletRequest.getRequestURI() + '?' + s).orElse(servletRequest.getRequestURI()));
        Request.Builder builder = new Request.Builder().url(getDomain() + finalUri);

        // headers
        builder.headers(getRequestHeaders(servletRequest));

        // requestBody
        String contentType = servletRequest.getContentType();
        MediaType mediaType = MediaType.get(Optional.ofNullable(contentType).orElse(DEFAULT_CONTENT_TYPE));
        RequestBody requestBody = RequestBody.create(body, mediaType);
        String method = servletRequest.getMethod();
        switch (method) {
            case "GET":
                builder.get();
                break;
            case "POST":
                builder.post(requestBody);
                break;
            case "PUT":
                builder.put(requestBody);
                break;
            case "PATCH":
                builder.patch(requestBody);
                break;
            case "DELETE":
                builder.delete(requestBody);
                break;
            default:
        }
        return builder.build();
    }

    @Override
    public ResponseEntity<byte[]> proxyGet(HttpServletRequest request, HttpServletResponse response, String uri) {
        Response proxyResponse = execute(buildGet(request, uri));
        if (null == proxyResponse) {
            return ResponseEntity.status(500).build();
        }
        return processResponse(proxyResponse, response);
    }

    private Request buildGet(HttpServletRequest request, String uri) {
        // uri
        final String finalUri = Optional.ofNullable(uri)
                .orElse(Optional.ofNullable(request.getQueryString())
                        .map(s -> request.getRequestURI() + '?' + s).orElse(request.getRequestURI()));
        Request.Builder builder = new Request.Builder().get().url(getDomain() + finalUri);

        // headers
        builder.headers(getRequestHeaders(request));
        return builder.build();
    }

    @Override
    public ResponseEntity<byte[]> proxy(HttpServletRequest request, HttpServletResponse response, String body, String uri) {
        Response proxyResponse = execute(buildRequest(request, body, uri));
        if (null == proxyResponse) {
            return ResponseEntity.status(500).build();
        }
        return processResponse(proxyResponse, response);
    }

    @Nullable
    private Response execute(Request request) {
        Response proxyResponse;
        try {
            proxyResponse = client.newCall(request).execute();
        } catch (IOException e) {
            log.error("Failed to execute request: {} {}!", request.method(),
                    request.url(), e);
            return null;
        }
        return proxyResponse;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    private ResponseEntity<byte[]> processResponse(Response proxyResponse, HttpServletResponse servletResponse) {
        Iterator<Pair<String, String>> iterator = proxyResponse.headers().iterator();
        //noinspection WhileLoopReplaceableByForEach
        while (iterator.hasNext()) {
            Pair<String, String> header = iterator.next();
            servletResponse.addHeader(header.getFirst(), header.getSecond());
        }
        ResponseBody body = proxyResponse.body();
        if (null == body) {
            return ResponseEntity.status(proxyResponse.code()).build();
        }
        byte[] bytes = new byte[0];
        try {
            bytes = body.bytes();
        } catch (IOException e) {
            log.error("Proxy failed when get bytes from response", e);
        }
        String contentType = Optional.ofNullable(body.contentType()).map(MediaType::toString).orElse(DEFAULT_CONTENT_TYPE);
        return ResponseEntity.status(HttpStatus.valueOf(proxyResponse.code()))
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .contentLength(body.contentLength())
                .body(bytes);
    }
}
