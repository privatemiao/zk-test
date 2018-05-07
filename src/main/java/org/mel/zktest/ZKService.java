package org.mel.zktest;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class ZKService {
    private ZKParams params = new ZKParams();

    private CuratorFramework client = null;

    @Value("${hostport}")
    private String host;

    @Value("${timeout}")
    private String timeout;

    @Value("${fixedlength}")
    private String fixedlength;

    @Value("${path}")
    private String path;
    @Value("${cache}")
    private String cache;
//    @Value("${ticketpath}")
//    private String ticketpath;

    @PostConstruct
    private void init() {
        loadConfig();
        start();
    }

    private void start() {
        client = CuratorFrameworkFactory.newClient(params.getHostPort(),
                new ExponentialBackoffRetry(params.getTimeout(), 3));
        client.start();
    }

    public CuratorFramework getClient() {
        return client;
    }

    @PreDestroy
    private void destroy() {
        if (client != null) {
            client.close();
        }
    }

    private void loadConfig() {
        params.setFixedLength(
                Integer.parseInt(fixedlength));
        params.setHostPort(host);
        if (StringUtils.isNotBlank(path)) {
            params.setPathPrefixed(path);
        }
        params.setSerialFormat(new StringBuilder().append("9%0").append(params.getFixedLength() - 1).append("d").toString());
        params.setTimeout(Integer.parseInt(timeout));
        params.setCache(Integer.parseInt(cache));
//        if (StringUtils.isNotBlank(ticketpath)) {
//            params.setTicketpath(ticketpath);
//        }
        System.out.println("\r\n\r\n\r\n\r\bConfig: " + params.toString());
    }

    public ZKParams getParams() {
        return params;
    }


}
