package me.wcc.ntlm.http;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.springframework.http.HttpMethod;

/**
 * @author chuncheng.wang@hand-china.com
 */
public class HttpDelete extends HttpEntityEnclosingRequestBase {
    public HttpDelete() {
        super();
    }

    @Override
    public String getMethod() {
        return HttpMethod.DELETE.name();
    }
}
