package org.mel.zktest;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TicketSerialCache {
	protected static final Logger logger = LoggerFactory.getLogger(TicketSerialCache.class);

	private DistributedAtomicInteger counter = null;

	private String path = null;

	private ZKParams params;

	public TicketSerialCache(CuratorFramework client, String path, ZKParams params) {
		this.path = path;
		this.params = params;
		this.counter = new DistributedAtomicInteger(client, path, new ExponentialBackoffRetry(params.getTimeout(), 3));
	}

	/**
	 * 
	 * @return null: caller should call back one more time.
	 * @throws Exception
	 */
	public Integer getSerial() throws Exception {
		AtomicValue<Integer> increment = counter.increment();
		if (!increment.succeeded()) {
			logger.debug("\r\nAtomic increment failure, will try one more time in 200 mllis.");
			return null;
		}

		Integer postValue = increment.postValue();
		validatePostValue(postValue);
		return postValue;
	}

	private void validatePostValue(Integer postValue) {
		if (String.valueOf(postValue).length() > params.getFixedLength() - 1) {
			logger.debug("\r\n\tPostValue is Sold out: {} [invalid]", postValue);
			throw new RuntimeException("Ticket Sold out.");
		}

	}

	@Override
	public String toString() {
		return String.format("Counter of %s.", path);
	}

}
