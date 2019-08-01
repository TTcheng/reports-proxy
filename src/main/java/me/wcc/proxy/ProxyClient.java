package me.wcc.proxy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;

/**
 * @author chuncheng.wang@hand-china.com 19-7-20 下午3:57
 */
public interface ProxyClient {

    /**
     * 代理get请求，保持uri保持不变
     *
     * @param request  HttpServletRequest
     * @param response HttpServletRequest
     * @return 字节响应体
     */
    default ResponseEntity<byte[]> getBytes(HttpServletRequest request, HttpServletResponse response) {
        return getBytes(request, response, null);
    }

    /**
     * 代理get请求，可指定uri
     *
     * @param request  HttpServletRequest
     * @param response HttpServletRequest
     * @param uri      指定uri
     * @return 字节响应体
     */
    ResponseEntity<byte[]> getBytes(HttpServletRequest request, HttpServletResponse response, String uri);

    /**
     * get请求并以字符串返回
     *
     * @param uri uri
     * @return String
     */
    String get(String uri);

    /**
     * 代理请求，保持uri保持不变
     *
     * @param request  HttpServletRequest
     * @param response HttpServletRequest
     * @param body     请求体
     * @return 字节响应体
     */
    default ResponseEntity<byte[]> proxy(HttpServletRequest request, HttpServletResponse response, String body) {
        return proxy(request, response, body, null);
    }

    /**
     * 代理请求，可指定uri
     *
     * @param request  HttpServletRequest
     * @param response HttpServletRequest
     * @param uri      指定uri
     * @param body     请求体
     * @return 字节响应体
     */
    ResponseEntity<byte[]> proxy(HttpServletRequest request, HttpServletResponse response, String body, String uri);

    /**
     * 获取域名
     *
     * @return 域名
     */
    String getDomain();
}
