package me.wcc.proxy.okhttp;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.EventListener;

/**
 * @author chuncheng.wang@hand-china.com 19-7-20 下午3:20
 */
@Slf4j
public class ReportsEventListener extends EventListener {
    private long callStartNanos;

    private void printEvent(String name) {
        long nowNanos = System.nanoTime();
        if (name.equals("callStart")) {
            callStartNanos = nowNanos;
        }
        long elapsedNanos = nowNanos - callStartNanos;
        log.debug(String.format("%.3f %s%n", elapsedNanos / 1000000000d, name));
    }

    @Override
    public void callStart(Call call) {
        printEvent("callStart");
    }

    @Override
    public void callEnd(Call call) {
        printEvent("callEnd");
    }
}
