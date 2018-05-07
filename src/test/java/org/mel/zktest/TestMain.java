package org.mel.zktest;

public class TestMain {

	public static void main(String[] args) throws Exception {
		/*CuratorFramework client = CuratorFrameworkFactory.newClient("10.100.103.13",
                new ExponentialBackoffRetry(5000, 3));
        client.start();
        String path="/ticket-serial/office-01";
        client.delete().deletingChildrenIfNeeded().forPath(path);
        client.close();*/
		
		System.out.println(String.format("%2s", "x").replaceAll(" ", "0"));
	}
}
