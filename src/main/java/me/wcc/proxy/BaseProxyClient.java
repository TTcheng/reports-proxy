package me.wcc.proxy;

import org.springframework.http.HttpHeaders;

/**
 * @author chuncheng.wang@hand-china.com 19-7-19 下午5:33
 */
public abstract class BaseProxyClient implements ProxyClient {
    /**
     * 判断请求头是否应该移除
     *
     * @param headerName 请求头名称
     * @return boolean
     */
    protected Boolean ifRemoveHeader(String headerName) {
        return HttpHeaders.AUTHORIZATION.equalsIgnoreCase(headerName)
                // content-length会重新计算，并且后面调的是addHeader，会造成centent-length重复
                || HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(headerName)
                || HttpHeaders.TRANSFER_ENCODING.equalsIgnoreCase(headerName);
    }
}
