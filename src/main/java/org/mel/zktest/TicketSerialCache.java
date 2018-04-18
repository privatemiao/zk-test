package org.mel.zktest;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TicketSerialCache {
	protected static final Logger logger = LoggerFactory.getLogger(TicketSerialCache.class);

	private DistributedAtomicLong counter = null;

	private String path = null;

	public TicketSerialCache(CuratorFramework client, String path, ZKParams params) {
		this.path = path;
		this.counter = new DistributedAtomicLong(client, path, new ExponentialBackoffRetry(params.getTimeout(), 3));
	}

	/**
	 * 
	 * @return null: caller should call back one more time.
	 * @throws Exception
	 */
	public Long getSerial() throws Exception {
		AtomicValue<Long> increment = counter.increment();
		if (!increment.succeeded()) {
			logger.debug("\r\nAtomic increment failure.");
			return null;
		}

		Long postValue = increment.postValue();
		return postValue;
	}

	@Override
	public String toString() {
		return String.format("Counter of %s.", path);
	}

}
