package me.wcc.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * @author chuncheng.wang@hand-china.com
 */
@Slf4j
public class PowerbiUtils {
    private static final String VIEW_STATE = "__VIEWSTATE";
    private static final String VIEW_STATE_GENERATOR = "__VIEWSTATEGENERATOR";
    private static final String EVENT_VALIDATION = "__EVENTVALIDATION";
    private static final String TXT_PWD = "TxtPwd";
    private static final String TXT_USER = "TxtUser";

    private static final String FIELD_VALUE = "value";
    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * GSON是线程安全的
     */
    private static final Gson GSON = new Gson();

    private PowerbiUtils() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    public static String authBody(String loginPage, String username, String password) {
        Map<String, String> paramsMap = basicFormAuth(loginPage, username, password);
        StringBuilder bodyBuilder = new StringBuilder();
        paramsMap.forEach((key, value) -> bodyBuilder.append('&').append("key=").append(value));
        String body = bodyBuilder.toString().substring(1);
        try {
            body = URLEncoder.encode(body, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            log.warn("Url-Encoding failed", e);
        }
        return body;
    }

    /**
     * @param html Logon-page string
     * @return form params
     */
    public static Map<String, String> basicFormAuth(String html, String username, String password) {
        Document document = Jsoup.parse(html);
        Element body = document.body();
        String viewState = body.getElementById(VIEW_STATE).attr(FIELD_VALUE);
        String viewStateGenerator = body.getElementById(VIEW_STATE_GENERATOR).attr(FIELD_VALUE);
        String eventValidation = body.getElementById(EVENT_VALIDATION).attr(FIELD_VALUE);
        Map<String, String> formParams = new HashMap<>(5);
        formParams.put(VIEW_STATE, viewState);
        formParams.put(VIEW_STATE_GENERATOR, viewStateGenerator);
        formParams.put(EVENT_VALIDATION, eventValidation);
        formParams.put(TXT_PWD, password);
        formParams.put(TXT_USER, username);
        return formParams;
    }

    public static String formAction(String html) {
        Element body = Jsoup.parse(html).body();
        String action = body.getElementById("Form1").attr("action");
        return action.substring(1);
    }

    public static String encodeUrl(String url) {
        try {
            return URLEncoder.encode(url, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            log.warn("Error occurred when encoding url {}", url, e);
        }
        return url;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(String json) {
        return GSON.fromJson(json, Map.class);
    }

    /**
     * 处理HttpResponse的响应信息，如果状态码是：
     * <ul>
     * <li>2xx：直接把body解析成字符串返回。</li>
     * <li>4xx和5xx：
     * <p>
     * 如果响应字符串为空，则抛出异常并指明响应码;否则首先尝试以这个格式解析
     * <code>{"error": {"code": errorCode,"message":"errorInfo"}</code>
     * ，成功则抛出异常并携带其中的errorInfo，不行的话抛出异常并携带整个响应字符串。
     * </p>
     * </li>
     * </ul>
     *
     * @param response HttpResponse
     * @return 响应字符串
     * @throws RuntimeException 封装错误信息 or 解析出错
     */
    public static String handleResponse(HttpResponse response) {
        String string = "";
        try {
            string = EntityUtils.toString(response.getEntity(), Charset.forName(DEFAULT_ENCODING));
        } catch (IOException e) {
            log.error("Failed to extract body from response!", e);
            return string;
        }
        // 成功
        int statusCode = response.getStatusLine().getStatusCode();
        if (success(statusCode)) {
            return string;
        }
        // 有错
        // 无响应体,记录错误的响应码
        if ("".equals(string)) {
            log.warn("Response code from report server{}", statusCode);
            return string;
        }
        // 尝试解析错误信息
        Map<String, Object> map = toMap(string);
        if (null != map) {
            Object error = map.get("error");
            if (null != error) {
                // 错误响应信息
                @SuppressWarnings("unchecked")
                Map<String, Object> errorMap = (Map<String, Object>) error;
                String errorInfo = (String) errorMap.get("message");
                log.warn(ERROR_WITH_CONTENT, errorInfo);
                return string;
            }
        }
        log.warn(ERROR_WITH_CONTENT, string);
        return string;
    }

    private static final String ERROR_WITH_CONTENT = "Error from report server{}";

    private static boolean success(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }
}
