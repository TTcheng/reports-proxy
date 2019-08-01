package me.wcc.proxy.apache;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import me.wcc.proxy.BaseProxyClient;
import me.wcc.util.PowerbiUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.*;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;

/**
 * @author chuncheng.wang@hand-china.com
 */
@Slf4j
@Contract(threading = ThreadingBehavior.SAFE)
@SuppressWarnings("unused")
public abstract class BaseApacheProxyClient extends BaseProxyClient {
    private static final String ACCEPT =
            "image/jpeg, application/x-ms-application, image/gif, application/xaml+xml, image/pjpeg, application/x-ms-xbap, */*";
    private static final String ACCEPT_ENCODING = "gzip, deflate, br";
    private static final String ACCEPT_LANG = "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3";
    private static final String DNT = "1";
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64; rv:67.0) Gecko/20100101 Firefox/67.0";

    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String DEFAULT_CONTENT_TYPE = "text/plain";

    protected static final Header[] DEFAULT_HEADERS = new Header[]{new BasicHeader(HttpHeaders.ACCEPT, ACCEPT),
            new BasicHeader(HttpHeaders.ACCEPT_ENCODING, ACCEPT_ENCODING),
            new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE, ACCEPT_LANG), new BasicHeader("DNT", DNT),
            new BasicHeader(HttpHeaders.USER_AGENT, USER_AGENT)};

    /**
     * 安全的类，实例可以共享
     */
    private CloseableHttpClient httpClient;
    private volatile CookieStore cookieStore;

    protected void init() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        cookieStore = new BasicCookieStore();
        credentialsProvider.setCredentials(AuthScope.ANY, getCredentials());
        httpClient = httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                .setDefaultCookieStore(cookieStore).build();
    }

    protected BaseApacheProxyClient() {
    }

    /**
     * 提供credentials
     *
     * @return credentials
     */
    abstract Credentials getCredentials();

    @Override
    public String get(String uri) {
        // 为保证线程安全，需要每次都创建一个实例
        HttpGet httpGet = new HttpGet();
        httpGet.setURI(URI.create(getDomain() + uri));
        HttpResponse response = execute(httpGet);
        return PowerbiUtils.handleResponse(response);
    }

    public HttpResponse postJson(String uri, String body) {
        HttpPost httpPost = new HttpPost();
        httpPost.setURI(URI.create(getDomain() + uri));
        HttpEntity entity = new ByteArrayEntity(body.getBytes(), ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        return execute(httpPost);
    }

    public HttpResponse delete(String uri) {
        me.wcc.http.HttpDelete httpDelete = new me.wcc.http.HttpDelete();
        httpDelete.setURI(URI.create(getDomain() + uri));
        return execute(httpDelete);
    }

    public List<Cookie> getCookies() {
        return cookieStore.getCookies();
    }

    /**
     * 代理get请求，以字节响应体返回。
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @return ResponseEntity<byte [ ]>
     */
    @Override
    public ResponseEntity<byte[]> getBytes(HttpServletRequest request, HttpServletResponse response,
                                           @Nullable String uri) {
        HttpUriRequest httpGet = prepareGet(request, uri);
        HttpResponse proxyResponse = execute(httpGet);
        return responseBytes(response, proxyResponse);
    }

    public ResponseEntity<String> get(HttpServletRequest request, HttpServletResponse response) {
        return get(request, response, null);
    }

    /**
     * 代理get请求，以字符串响应体返回。
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param uri      请求uri
     * @return ResponseEntity<String>
     */
    public ResponseEntity<String> get(HttpServletRequest request, HttpServletResponse response, @Nullable String uri) {
        HttpUriRequest httpGet = prepareGet(request, uri);
        HttpResponse proxyResponse = execute(httpGet);
        return responseString(response, proxyResponse);
    }

    /**
     * 代理请求，以字节响应体返回。
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @param body     请求体
     * @return ResponseEntity<String>
     */
    public ResponseEntity<byte[]> proxyWantBytes(HttpServletRequest request, HttpServletResponse response,
                                                 String body) {
        HttpUriRequest preparedRequest = prepareRequest(request, body, null);
        HttpResponse proxyResponse = execute(preparedRequest);
        return responseBytes(response, proxyResponse);
    }

    /**
     * 代理请求，以字符串响应体返回
     *
     * @param request  前端请求
     * @param response 前端响应
     * @param body     请求体
     * @param uri      请求uri
     * @return ResponseEntity<String>
     */
    @Override
    public ResponseEntity<byte[]> proxy(HttpServletRequest request, HttpServletResponse response, String body,
                                        @Nullable String uri) {
        HttpUriRequest preparedRequest = prepareRequest(request, body, uri);
        HttpResponse proxyResponse = execute(preparedRequest);
        return responseBytes(response, proxyResponse);
    }

    /**
     * 将代理的响应{@link HttpResponse}转化为{@link ResponseEntity<byte[]>}的响应体
     *
     * @return ResponseEntity<byte [ ]>
     */
    private ResponseEntity<byte[]> responseBytes(HttpServletResponse servletResponse, HttpResponse proxyResponse) {
        if (null == proxyResponse) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        // 响应体
        byte[] bytes = new byte[0];
        HttpEntity responseEntity = proxyResponse.getEntity();
        if (null != responseEntity) {
            try {
                bytes = EntityUtils.toByteArray(responseEntity);
            } catch (IOException e) {
                log.warn("parse error", e);
            }
        }

        // 响应头
        Header[] allHeaders = proxyResponse.getAllHeaders();
        for (Header header : allHeaders) {
            if ("X-Frame-Options".equalsIgnoreCase(header.getName())){
                continue;
            }
            servletResponse.setHeader(header.getName(), header.getValue());
        }
        int statusCode = proxyResponse.getStatusLine().getStatusCode();
        String contentType = DEFAULT_CONTENT_TYPE;
        long contentLength = -1L;
        if (null != responseEntity) {
            if (null != responseEntity.getContentType()) {
                contentType = responseEntity.getContentType().getValue();
            }
            contentLength = responseEntity.getContentLength();
        }
        return ResponseEntity.status(HttpStatus.valueOf(statusCode)).contentType(MediaType.parseMediaType(contentType))
                .contentLength(contentLength).body(bytes);
    }

    /**
     * 将代理的响应{@link HttpResponse}转化为{@link ResponseEntity<String>}的响应体
     *
     * @return ResponseEntity<String>
     */
    private ResponseEntity<String> responseString(HttpServletResponse servletResponse, HttpResponse proxyResponse) {
        ResponseEntity<byte[]> responseBytes = responseBytes(servletResponse, proxyResponse);
        String strBody = new String(Objects.requireNonNull(responseBytes.getBody()));
        return ResponseEntity.status(responseBytes.getStatusCode()).headers(responseBytes.getHeaders()).body(strBody);
    }

    private HttpUriRequest prepareRequest(HttpServletRequest request, @Nullable String body, @Nullable String uri) {
        // 请求体转换
        HttpEntity entity = null;
        if (null != body) {
            entity = new StringEntity(body, Charset.forName(DEFAULT_ENCODING));
        }

        // 请求头
        List<Header> headers = getAllRequestHeaderList(request);
        // URI和及其请求参数
        // 指定uri时使用指定值，否则从HttpServletRequest中取
        String targetUri = Optional.ofNullable(uri).filter(StringUtils::isNotBlank)
                .orElse(Optional.ofNullable(request.getQueryString())
                        .map(s -> request.getRequestURI() + '?' + s)
                        .orElse(request.getRequestURI()));
        log.debug("body: {}", body);
        final Header[] finalHeaders = new Header[headers.size()];
        headers.toArray(finalHeaders);
        switch (request.getMethod().toUpperCase()) {
            case "POST":
                return preparePost(targetUri, entity, finalHeaders);
            case "PUT":
                return preparePut(targetUri, entity, finalHeaders);
            case "PATCH":
                return preparePatch(targetUri, entity, finalHeaders);
            case "DELETE":
                return prepareDelete(targetUri, entity, finalHeaders);
            case "OPTIONS":
                return prepareOptions(targetUri, finalHeaders);
            default:
                return prepareGet(targetUri, finalHeaders);
        }
    }

    private HttpUriRequest prepareGet(HttpServletRequest request, @Nullable String uri) {
        // URI和及其请求参数
        // 指定uri时使用指定值，否则从HttpServletRequest中取
        String targetUri = uri;
        if (StringUtils.isBlank(uri)) {
            String originUri = request.getRequestURI();
            String queryString = Optional.ofNullable(request.getQueryString()).orElse("");
            // 可能存在不支持的请求参数param={"name":"value"},所以要编码查询参数
            targetUri = Optional.of(queryString).filter(StringUtils::isNotBlank)
                    .map(s -> originUri + '?' + s).orElse(originUri);
        }
        // 为保证线程安全，需要每次都创建一个实例
        HttpGet httpGet = new HttpGet();
        httpGet.setHeaders(getAllRequestHeaderArray(request));
        httpGet.setURI(URI.create(getDomain() + targetUri));
        return httpGet;
    }

    private HttpUriRequest prepareGet(String uri, Header[] headers) {
        // 为保证线程安全，需要每次都创建一个实例
        HttpGet httpGet = new HttpGet();
        httpGet.setHeaders(headers);
        httpGet.setURI(URI.create(getDomain() + uri));
        return httpGet;
    }

    private HttpUriRequest preparePost(String uri, HttpEntity entity, Header[] headers) {
        HttpPost httpPost = new HttpPost();
        httpPost.setHeaders(headers);
        httpPost.setURI(URI.create(getDomain() + uri));
        httpPost.setEntity(entity);
        return httpPost;
    }

    private HttpUriRequest preparePut(String uri, HttpEntity entity, Header[] headers) {
        HttpPut httpPut = new HttpPut();
        httpPut.setHeaders(headers);
        httpPut.setURI(URI.create(getDomain() + uri));
        httpPut.setEntity(entity);
        return httpPut;
    }

    private HttpUriRequest prepareDelete(String uri, HttpEntity entity, Header[] headers) {
        me.wcc.http.HttpDelete httpDelete = new me.wcc.http.HttpDelete();
        httpDelete.setHeaders(headers);
        httpDelete.setURI(URI.create(getDomain() + uri));
        httpDelete.setEntity(entity);
        return httpDelete;
    }

    private HttpUriRequest prepareOptions(String uri, Header[] headers) {
        HttpOptions httpOptions = new HttpOptions();
        httpOptions.setHeaders(headers);
        httpOptions.setURI(URI.create(getDomain() + uri));
        return httpOptions;
    }

    private HttpUriRequest preparePatch(String uri, HttpEntity entity, Header[] headers) {
        HttpPatch httpPatch = new HttpPatch();
        httpPatch.setHeaders(headers);
        httpPatch.setURI(URI.create(getDomain() + uri));
        httpPatch.setEntity(entity);
        return httpPatch;
    }


    /**
     * 执行请求之前，方便做最后配置
     *
     * @param httpRequest 请求
     */
    abstract void beforeExecute(HttpUriRequest httpRequest);

    private List<Header> getAllRequestHeaderList(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        List<Header> headers = new ArrayList<>();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            Header header = new BasicHeader(name, request.getHeader(name));
            headers.add(header);
        }
        headers.removeIf(header -> ifRemoveHeader(header.getName()));
        return headers;
    }

    private Header[] getAllRequestHeaderArray(HttpServletRequest request) {
        List<Header> headerList = getAllRequestHeaderList(request);
        final Header[] headerArray = new Header[headerList.size()];
        headerList.toArray(headerArray);
        return headerArray;
    }

    public HttpResponse execute(HttpUriRequest httpRequest) {
        log.debug("{} {}", httpRequest.getMethod(), httpRequest.getURI());
        beforeExecute(httpRequest);
        try {
            return httpClient.execute(httpRequest);
        } catch (IOException e) {
            log.error("{} {} failed", httpRequest.getMethod(), httpRequest.getURI(), e);
        }
        return null;
    }
}
