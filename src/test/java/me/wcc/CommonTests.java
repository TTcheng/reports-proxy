package me.wcc;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import me.wcc.proxy.apache.BasicHttpClientReportsProxy;
import me.wcc.proxy.apache.NtlmHttpClientReportsProxy;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * @author chuncheng.wang@hand-china.com
 */
@SuppressWarnings("all")
public class CommonTests {
    @Test
    public void testHttpMethod() {
        assertEquals(HttpMethod.GET.name(), "GET");
        System.out.println(HttpMethod.GET.name());
    }

    @Test
    public void testNtlm() throws IOException {
        NtlmHttpClientReportsProxy jesse = new NtlmHttpClientReportsProxy("jesse", "H@ndDBA", "http://192.168.12.158", "");
        System.out.println(jesse.getCookies().toString());

        String response = jesse.get("/reports/api/v2.0/me");
        System.out.println("=======================");
        System.out.println(response);
        System.out.println("=======================");
        System.out.println(jesse.getCookies());
    }

    /**
     * 测试BasicClient是否是线程安全的
     */
    @Test
    public void testMultiThreadClient() throws IOException, InterruptedException {
        final String domain = "http://192.168.12.158";
        BasicHttpClientReportsProxy jesse = new BasicHttpClientReportsProxy("jesse", "H@ndDBA", domain);
        Thread thread1 = new Thread(() -> {
            HttpGet httpGet = new HttpGet();
            httpGet.setURI(URI.create(domain + "/reports/api/v2.0/me"));
            HttpResponse me = jesse.execute(httpGet);
            String meStr = null;
            try {
                meStr = EntityUtils.toString(me.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(meStr);
            System.out.println("111111111111111111111111111111111111111111");
        });
        thread1.start();
        Thread thread2 = new Thread(() -> {
            HttpGet httpGet = new HttpGet();
            httpGet.setURI(URI.create(domain + "/Reports/api/v2.0/CatalogItems(Path='/hdsp')"));
            HttpResponse folder = jesse.execute(httpGet);
            String folderStr = null;
            try {
                folderStr = EntityUtils.toString(folder.getEntity());
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(folderStr);
            System.out.println("22222222222222222222222222222222222222222");
        });
        thread2.start();
        Thread.sleep(5000);
    }

    @Test
    public void testBasic() throws IOException {
        BasicHttpClientReportsProxy jesse = new BasicHttpClientReportsProxy("jesse", "H@ndDBA", "http://192.168.12.158");
        System.out.println(jesse.getCookies().toString());

        String results = jesse.get("/reports/api/v2.0/me");
        System.out.println("=======================");
        System.out.println(results);
        System.out.println("=======================");
        System.out.println(jesse.getCookies());
    }

    @Test
    public void testUri() {
        String string = "ReturnUrl=%2fReportServer%2flocalredirect%3furl%3d%252freports%252fpowerbi%252flicense%253frs%253aEmbed%253dtrue";
        int index = string.indexOf("reports");
        String substring = string.substring(index);
        System.out.println(substring);
    }

    @Test
    public void testEncodeUrl() {
        String origin = "name=jesse&pwd=1234";
        try {
            String encode = URLEncoder.encode(origin, "UTF-8");
            System.out.println(encode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAnyLength() {
        String encode = "__VIEWSTATE=%2BNu9BbH2nzm%2BryLbXVm7BtTRlYAy84eXbmFiwxFiK7PVqUGj4pixxn7mqWmKHadhDRxbPsQiHfFKzTSdNEO0Et8jiC39ycKb9sj21Y%2Bd%2B73KU%2FuIoCT1pGZtvnwbPKdPaparww%3D%3D&__VIEWSTATEGENERATOR=1910D77E&__EVENTVALIDATION=2eypFz6Diea2rOTgpdKkNjerk0ugGn0tD3kpNsMxDR032mNZa%2BmOSA3HFQWwgtlt%2BZ2OowHF42%2BSciZJ2nw06xF8hytLGRqTXnrhTiQQZN07%2B9yRSB46BF9d2FJXityg8pRL%2FQWMb50h9wjwnAbJ7GVft4ar8gaW%2FabImm4AkCTmrmDh%2FtcTNmfNDrj%2BRKBnfZUmoObHsGwyYb859W%2B6q29VA0w%3D&BtnLogon=Logon&TxtPwd=123123&TxtUser=jesse";
        assertEquals(494, encode.length());
        System.out.println(encode.length());

        String str = "http://192.168.12.158/powerbi/?id=a1f874e4-c408-4281-8bb0-80343912e823&formatLocale=zh-CN&hostdata={%22Build%22:%2215.0.1102.299%22,%22ExternalUser%22:%22True%22,%22IsPublicBuild%22:true,%22Host%22:%22Microsoft.ReportingServices.Portal.Services%22,%22HashedUserId%22:%223F2E23B2EE1991DA34BDC689A44A229D1B64F3B617D6569FFB6E19F760ACFDED%22,%22InstallationId%22:%22daf6ef0d-1736-441c-b655-324d159d106e%22,%22IsEnabled%22:true,%22Edition%22:%22PBIRS%20Developer%22,%22AuthenticationTypes%22:%22Custom%22,%22NumberOfProcessors%22:4,%22NumberOfCores%22:4,%22IsVirtualMachine%22:false,%22MachineId%22:%220AD98E6916ACA3763FC4CB28103121DF%22,%22CountInstances%22:1,%22Count14xInstances%22:0,%22Count13xInstances%22:0,%22Count12xInstances%22:0,%22Count11xInstances%22:0,%22ProductSku%22:%22SSRSPBI%22}";
        char c = str.charAt(99);
        System.out.println(c);
    }


}
