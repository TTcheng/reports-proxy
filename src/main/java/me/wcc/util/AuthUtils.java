package me.wcc.util;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * @author chuncheng.wang@hand-china.com
 */
public class AuthUtils {
    private static final String VIEW_STATE = "__VIEWSTATE";
    private static final String VIEW_STATE_GENERATOR = "__VIEWSTATEGENERATOR";
    private static final String EVENT_VALIDATION = "__EVENTVALIDATION";
    private static final String TXT_PWD = "TxtPwd";
    private static final String TXT_USER = "TxtUser";

    private static final String FIELD_VALUE = "value";

    private AuthUtils() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    /**
     * @param html Logon-page string
     * @return form params
     */
    public static Map<String, String> basicFormAuth(String html) {
        Document document = Jsoup.parse(html);
        Element body = document.body();
        String viewState = body.getElementById(VIEW_STATE).attr(FIELD_VALUE);
        String viewStateGenerator = body.getElementById(VIEW_STATE_GENERATOR).attr(FIELD_VALUE);
        String eventValidation = body.getElementById(EVENT_VALIDATION).attr(FIELD_VALUE);
        Map<String, String> formParams = new HashMap<>(5);
        formParams.put(VIEW_STATE, viewState);
        formParams.put(VIEW_STATE_GENERATOR, viewStateGenerator);
        formParams.put(EVENT_VALIDATION, eventValidation);
        formParams.put(TXT_PWD, "H@ndDBA");
        formParams.put(TXT_USER, "jesse");
        return formParams;
    }

    public static String formAction(String html){
        Element body = Jsoup.parse(html).body();
        String action = body.getElementById("Form1").attr("action");
        return action.substring(1);
    }
}
