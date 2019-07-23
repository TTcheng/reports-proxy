package me.wcc.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * @author chuncheng.wang@hand-china.com
 */
@Slf4j
public class PowerbiAuthUtils {
    private static final String VIEW_STATE = "__VIEWSTATE";
    private static final String VIEW_STATE_GENERATOR = "__VIEWSTATEGENERATOR";
    private static final String EVENT_VALIDATION = "__EVENTVALIDATION";
    private static final String TXT_PWD = "TxtPwd";
    private static final String TXT_USER = "TxtUser";

    private static final String FIELD_VALUE = "value";
    public static final String DEFAULT_ENCODING = "UTF-8";

    private PowerbiAuthUtils() throws IllegalAccessException {
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
}
