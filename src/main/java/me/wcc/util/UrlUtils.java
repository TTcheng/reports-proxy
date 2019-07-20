package me.wcc.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URLEncodedUtils;

/**
 * @author chuncheng.wang@hand-china.com 19-7-15 下午1:00
 */
@Slf4j
public class UrlUtils extends URLEncodedUtils {
    public static String getUrlParam(String queryString, String paramName) {
        String decode = queryString;
        try {
            decode = URLDecoder.decode(queryString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.warn("queryString decode error: {}", queryString, e);
        }
        String[] params = decode.split("&");
        for (String param : params) {
            if (param.startsWith(paramName)) {
                return param.split("=")[1];
            }
        }
        return null;
    }
}
