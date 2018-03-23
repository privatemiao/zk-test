package org.mel.zktest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TicketSerialService {
    private static final Logger logger = LoggerFactory.getLogger(TicketSerialService.class);

    private ZKParams params;

    private Map<String, TicketSerialCache> cache = new HashMap();

    private ExecutorService healthCheckThread;

    @Value("${singlewindows}")
    private boolean single;

    @Autowired
    private ZKService zkService;

    @Autowired
    private TicketNoDAO ticketNoDAO;

    @PostConstruct
    protected void init() throws IOException {
        params = zkService.getParams();

//        healthCheck();
        if (single) {
            testSingleWindow();
        } else {
            testMultipleWindow();
        }
    }

    @PreDestroy
    protected void close() {
        if (healthCheckThread != null) {
            healthCheckThread.shutdown();
        }
        if (testThread != null) {
            testThread.shutdownNow();
        }
    }

    /**
     * 获取指定窗口下当日累计数。<strong>不能作为统计数据使用</strong>。
     * <p>
     * <p>
     * 该方法提供特定窗口当日累计增长数据， 每天重新计算。每天刪除<i>CurrentDay-2</i>的数据。
     * 返回的数据格式为7位整形，不足7位用<i>0</i>填充。长度可以通过配置文件配置。
     * </p>
     * <p style="color:#f00;">
     * <strong>注意</strong><br>
     * 该功能为初版：供开发使用，后续将对性能进行升级，但调用方式不会发生变化。<br>
     * <hr>
     * 使用建议：该版方法未采用缓存方式，所以并发调用对性能影响较大，建议分散调用频率，比如，在刚生成存根时进行调用。
     * </p>
     *
     * @param officeNo 售处
     * @param windowNo 窗口号
     * @return %07d
     * @throws Exception
     */
    public String getTicketSerial(String officeNo, String windowNo) throws Exception {
        if (StringUtils.isBlank(officeNo) || StringUtils.isBlank(windowNo)) {
            logger.debug("\n\tParameters error [officeNo={}, windowNo={}].", officeNo, windowNo);
            throw new IllegalArgumentException("Parameters error");
        }
        String path = StringUtils.join(Arrays.asList(params.getPathPrefixed(), officeNo, windowNo), '/');
        Integer serial = null;
        serial = getWindowCache(path).getSerial();
        String result = serial == null ? null : String.format(params.getSerialFormat(), serial);
        logger.debug("{}/{}/{} Serial: {}", params.getPathPrefixed(), officeNo, windowNo, result);
        ticketNoDAO.save(new TicketNo(String.format("%s/%s/%s", officeNo, windowNo, result)));
        return result;
    }

    private TicketSerialCache getWindowCache(String path) {
        TicketSerialCache serialCache = cache.get(path);
        return serialCache != null ? serialCache : _getWindowCache(path);
    }

    private synchronized TicketSerialCache _getWindowCache(String path) {
        TicketSerialCache serialCache = cache.get(path);
        if (serialCache == null) {
            serialCache = new TicketSerialCache(zkService.getClient(), path, params);
            cache.put(path, serialCache);
        }
        return serialCache;
    }

    protected void healthCheck() {
        healthCheckThread = Executors.newFixedThreadPool(1);

        healthCheckThread.execute(new Runnable() {

            @Override
            public void run() {

                while (true) {
                    StringBuilder s = new StringBuilder();
                    s.append("\nTicketSerialService Cache:");

                    if (cache.isEmpty()) {
                        s.append("\n\tTicketSerialService.cache is empty.");
                    } else {
                        Iterator<String> i = cache.keySet().iterator();
                        int index = 0;
                        while (i.hasNext()) {
                            s.append("\n\t").append(++index).append(". ").append(cache.get(i.next()));
                        }
                        s.append("\n\t\t\t\tTotal of counters: ").append(index).append(".");
                    }
                    logger.debug(s.toString());
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

    }

    private ExecutorService testThread;

    public void testMultipleWindow() throws IOException {
        String[][] pathList = {
                {"1", "A"},
                {"1", "B"},
                {"2", "A"},
                {"2", "B"},
                {"3", "A"},
                {"3", "B"}};

        testThread = Executors.newFixedThreadPool(pathList.length);
        for (int i = 0; i < pathList.length; i++) {
            testThread.execute(new FetchDataTest(pathList[i]));
        }

    }

    public void testSingleWindow() {
        String[] path = {"10", "A"};
        logger.debug("单窗口 6个");

        testThread = Executors.newFixedThreadPool(6);
        for (int i = 0; i < 10; i++) {
            testThread.execute(new FetchDataTest(path, (i++)));
        }

    }

    class FetchDataTest implements Runnable {
        String[] path;
        int i = -1;

        public FetchDataTest(String[] path) {
            this.path = path;
        }

        public FetchDataTest(String[] path, int i) {
            this.path = path;
            this.i = i;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    String ticketSerial = getTicketSerial(path[0], path[1]);
                    logger.debug("\n----------------------\n{}/{}/{} - [{}] {}", params.getPathPrefixed(), path[0],
                            path[1], ticketSerial, i == -1 ? "" : "- " + i);
                    Thread.sleep(100);
                } catch (Exception e) {
                    logger.error("获取指定窗口下当日累计数失败！", e);
                    e.printStackTrace();
                    close();
                }
            }
        }

    }
}
