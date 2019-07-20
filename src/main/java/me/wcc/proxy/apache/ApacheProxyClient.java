package me.wcc.proxy.apache;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import me.wcc.http.HttpDelete;
import me.wcc.proxy.BaseProxyClient;
import me.wcc.util.UrlUtils;
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
public abstract class ApacheProxyClient extends BaseProxyClient {
    private static final String ACCEPT = "image/jpeg, application/x-ms-application, image/gif, application/xaml+xml, image/pjpeg, application/x-ms-xbap, */*";
    private static final String ACCEPT_ENCODING = "gzip, deflate, br";
    private static final String ACCEPT_LANG = "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3";
    private static final String DNT = "1";
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64; rv:67.0) Gecko/20100101 Firefox/67.0";

    private static final String DEFAULT_CONTENT_TYPE = "text/plain";
    // Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.2; WOW64; Trident/6.0; .NET4.0E; .NET4.0C; .NET CLR 3.5.30729; .NET CLR 2.0.50727; .NET CLR 3.0.30729)

    public static final Header[] HEADERS = new Header[]{
            new BasicHeader(HttpHeaders.ACCEPT, ACCEPT),
            new BasicHeader(HttpHeaders.ACCEPT_ENCODING, ACCEPT_ENCODING),
            new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE, ACCEPT_LANG),
            new BasicHeader("DNT", DNT),
            new BasicHeader(HttpHeaders.USER_AGENT, USER_AGENT)
    };

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
                .setDefaultCookieStore(cookieStore)
                .build();
    }

    protected ApacheProxyClient() {
    }

    /**
     * 提供credentials
     *
     * @return credentials
     */
    abstract Credentials getCredentials();

    public HttpResponse get(String uri, Header[] headers) {
        // 为保证线程安全，需要每次都创建一个实例
        HttpGet httpGet = new HttpGet();
        httpGet.setHeaders(headers);
        httpGet.setURI(URI.create(getDomain() + uri));
        return execute(httpGet);
    }

    public HttpResponse post(String uri, HttpEntity entity, Header[] headers) {
        HttpPost httpPost = new HttpPost();
        httpPost.setHeaders(headers);
        httpPost.setURI(URI.create(getDomain() + uri));
        httpPost.setEntity(entity);
        return execute(httpPost);
    }

    public HttpResponse put(String uri, HttpEntity entity, Header[] headers) {
        HttpPut httpPut = new HttpPut();
        httpPut.setHeaders(headers);
        httpPut.setURI(URI.create(getDomain() + uri));
        httpPut.setEntity(entity);
        return execute(httpPut);
    }

    public HttpResponse delete(String uri, HttpEntity entity, Header[] headers) {
        me.wcc.http.HttpDelete httpDelete = new HttpDelete();
        httpDelete.setHeaders(headers);
        httpDelete.setURI(URI.create(getDomain() + uri));
        httpDelete.setEntity(entity);
        return execute(httpDelete);
    }

    public HttpResponse options(String uri, Header[] headers) {
        HttpOptions httpOptions = new HttpOptions();
        httpOptions.setHeaders(headers);
        httpOptions.setURI(URI.create(getDomain() + uri));
        return execute(httpOptions);
    }

    public HttpResponse patch(String uri, HttpEntity entity, Header[] headers) {
        HttpPatch httpPatch = new HttpPatch();
        httpPatch.setHeaders(headers);
        httpPatch.setURI(URI.create(getDomain() + uri));
        httpPatch.setEntity(entity);
        return execute(httpPatch);
    }

    public List<Cookie> getCookies() {
        return cookieStore.getCookies();
    }

    @Override
    public ResponseEntity<byte[]> proxyGet(HttpServletRequest request, HttpServletResponse response, String uri) {
        Header[] headers = getAllRequestHeaderArray(request);
        HttpResponse httpResponse = get(uri, headers);
        return processResponse(httpResponse, request, response);
    }

    @Override
    public ResponseEntity<byte[]> proxy(HttpServletRequest request, HttpServletResponse response, String body, String uri) {
        // 请求体转换
        HttpEntity entity = null;
        if (null != body) {
            entity = new StringEntity(body, Charset.forName("UTF-8"));
        }

        // 请求头
        List<Header> headers = getAllRequestHeaderList(request);
        if (null != entity) {
            headers.removeIf(header -> header.getName().trim().toLowerCase().startsWith("content-length"));
        }
        // URI和及其请求参数
        String targetUri = uri;
        if (null == uri) {
            String queryString = Optional.ofNullable(request.getQueryString()).orElse("");
            String originUri = request.getRequestURI();
            if (queryString.startsWith("id=") && originUri.startsWith("/powerbi/")) {
                UrlUtils.getUrlParam(queryString, "host");
                try {
                    queryString = URLEncoder.encode(queryString, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    log.warn("Failed to Encode unescaped queryString: {}", queryString, e);
                }
            }
            targetUri = originUri + '?' + queryString;
        }

        // 代理执行请求
        HttpResponse httpResponse = execute(request.getMethod(), targetUri, entity, headers);
        return processResponse(httpResponse, request, response);
    }

    private ResponseEntity<byte[]> processResponse(HttpResponse httpResponse, HttpServletRequest servletRequest,
                                                   HttpServletResponse servletResponse) {
        if (null == httpResponse) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        // 响应体
        byte[] bytes = new byte[0];
        HttpEntity responseEntity = httpResponse.getEntity();
        if (null != responseEntity) {
            try {
                bytes = EntityUtils.toByteArray(responseEntity);
            } catch (IOException e) {
                log.warn("parse error", e);
            }
        }

        // 响应头
        Header[] allHeaders = httpResponse.getAllHeaders();
        for (Header header : allHeaders) {
            servletResponse.setHeader(header.getName(), header.getValue());
        }
        servletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, servletRequest.getHeader(HttpHeaders.ORIGIN));
        servletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        String contentType = DEFAULT_CONTENT_TYPE;
        long contentLength = -1L;
        if (null != responseEntity) {
            if (null != responseEntity.getContentType()) {
                contentType = responseEntity.getContentType().getValue();
            }
            contentLength = responseEntity.getContentLength();
        }
        return ResponseEntity.status(HttpStatus.valueOf(statusCode))
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(contentLength)
                .body(bytes);
    }

    public HttpResponse execute(String method, String uri, @Nullable HttpEntity entity, List<Header> headers) {
        final Header[] finalHeaders = new Header[headers.size()];
        headers.toArray(finalHeaders);
        if (log.isDebugEnabled()) {
            try {
                log.debug("{} {}, body:{}", method, uri, EntityUtils.toString(entity));
            } catch (IOException e) {
                log.debug("{} {}", method, uri);
            }
        }
        switch (method.toUpperCase()) {
            case "GET":
                return get(uri, finalHeaders);
            case "POST":
                return post(uri, entity, finalHeaders);
            case "PUT":
                return put(uri, entity, finalHeaders);
            case "PATCH":
                return patch(uri, entity, finalHeaders);
            case "DELETE":
                return delete(uri, entity, finalHeaders);
            case "OPTIONS":
                return options(uri, finalHeaders);
            default:
                return null;
        }
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
        beforeExecute(httpRequest);
        try {
            return httpClient.execute(httpRequest);
        } catch (IOException e) {
            log.error("{} {} failed", httpRequest.getMethod(), httpRequest.getURI(), e);
        }
        return null;
    }
}