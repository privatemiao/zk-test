package org.mel.zktest;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TicketSerialCache {
	protected static final Logger logger = LoggerFactory.getLogger(TicketSerialCache.class);

	private DistributedAtomicInteger counter = null;

	private String path = null;

	private ZKParams params;

	private CuratorFramework client;

	public TicketSerialCache(CuratorFramework client, String path, ZKParams params) {
		this.path = path;
		this.params = params;
		this.client = client;
		this.counter = new DistributedAtomicInteger(client, path, new ExponentialBackoffRetry(params.getTimeout(), 3));
	}

	/**
	 * 
	 * @return null: caller should call back one more time.
	 * @throws Exception
	 */
	public Integer getSerial() throws Exception {
		// 检查日期，每天重置
		if (isNextDay()) {
			logger.debug("\r\nNext day check not pass:");
			resetPath();
			return null;
		}
		
		AtomicValue<Integer> increment = counter.increment();
		if (!increment.succeeded()) {
			logger.debug("\r\nAtomic increment failure.");
			return null;
		}

		Integer postValue = increment.postValue();
		if (!validatePostValue(postValue)) {
			logger.debug("\r\nPostValue validate not pass: ");
			resetPath();
			return null;
		}
		return postValue;
	}

	private void resetPath() throws Exception {
		logger.debug("\r\n~~~~~~~~~~~~~~~~~~~~Force Set value to 0~~~~~~~~~~~~~~~~~~~~~~");
		counter.forceSet(0);
	}

	private boolean validatePostValue(Integer postValue) {
		if (String.valueOf(postValue).length() > params.getFixedLength() - 1) {
			logger.debug("\r\n\tPostValue is: {} [invalid]", postValue);
			return false;
		}
		return true;
	}

	private boolean isNextDay() throws Exception {
		DateTime currentTime = new DateTime();
		Stat stat = this.client.checkExists().forPath(this.path);
		if (stat == null) {
			logger.debug("\r\nCheck next day pass, the path [{}] not create yet.", this.path);
			return false;
		}
		DateTime lastTime = new DateTime(stat.getMtime());

		if (!lastTime.withTimeAtStartOfDay().isEqual(currentTime.withTimeAtStartOfDay())) {
			logger.debug("\r\n\tlastDayOnMonth: {}\tcurrentDayOfMonth: {}", lastTime.getDayOfMonth(),
					currentTime.getDayOfMonth());
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("Counter of %s.", path);
	}

}
