package org.mel.zktest;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class TestMain {

	public static void main(String[] args) throws Exception {
		CuratorFramework client = CuratorFrameworkFactory.newClient("10.100.103.13",
                new ExponentialBackoffRetry(5000, 3));
        client.start();
        String path="/ticket-serial/office-01";
        client.delete().deletingChildrenIfNeeded().forPath(path);
        client.close();
	}
}
