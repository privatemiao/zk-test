package org.mel.zktest;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TicketSerialCache {
	protected static final Logger logger = LoggerFactory.getLogger(TicketSerialCache.class);

	private DistributedAtomicInteger counter = null;

	private String path = null;

	private ZKParams params;

	public TicketSerialCache(CuratorFramework client, String path,  ZKParams params) {
		this.path = path;
		this.params = params;
		this.counter = new DistributedAtomicInteger(client, path, new ExponentialBackoffRetry(params.getTimeout(), 3));
	}

	public synchronized Integer getSerial() throws Exception {
		AtomicValue<Integer> increment = counter.increment();
		Integer postValue = increment.postValue();
		if (checkPostValue(postValue)){
			return postValue;
		}else{
			return getSerial();
		}
	}

	private boolean checkPostValue(Integer postValue) throws Exception {
		if (String.valueOf(postValue).length() > params.getFixedLength() || isNextDay()){
			counter.forceSet(0);
			return false;
		}
		return true;
	}

	private DateTime lastTime;

	private boolean isNextDay() {
		DateTime currentTime = new DateTime();
		if (lastTime == null){
			lastTime = currentTime;
			return false;
		}

		if (lastTime.getDayOfMonth() != currentTime.getDayOfMonth()){
			lastTime = currentTime;
			return true;
		}

		return false;

	}

	@Override
	public String toString() {
		return String.format("Counter of %s.", path);
	}

}
