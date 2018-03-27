package org.mel.zktest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ZKParams {
	public static final String HOST_PORT = "hostport";
	public static final String TIMEOUT = "timeout";
	public static final String FIXED_LENGTH = "fixedlength";
	public static final String PATH_PREFIXED = "path";
	public static final String CACHE = "cache";

	private String hostPort = "10.100.103.13:2181";
	private int timeout = 5000;
	private int fixedLength = 7;
	private String serialFormat = "9%06d";
	private String pathPrefixed = "/ticket-serial";
	private String ticketpath = "/ticket-core-master";
	private int cache = 10;

	public ZKParams() {
	}


	public int getCache() {
		return cache;
	}

	public void setCache(int cache) {
		this.cache = cache;
	}

	public String getHostPort() {
		return hostPort;
	}

	public void setHostPort(String hostPort) {
		this.hostPort = hostPort;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getFixedLength() {
		return fixedLength;
	}

	public void setFixedLength(int fixedLength) {
		this.fixedLength = fixedLength;
	}

	public String getSerialFormat() {
		return serialFormat;
	}

	public void setSerialFormat(String serialFormat) {
		this.serialFormat = serialFormat;
	}

	public String getPathPrefixed() {
		return pathPrefixed;
	}

	public void setPathPrefixed(String pathPrefixed) {
		this.pathPrefixed = pathPrefixed;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public String getTicketpath() {
		return ticketpath;
	}

	public void setTicketpath(String ticketpath) {
		if (StringUtils.isNotBlank(ticketpath)) {
			this.ticketpath = ticketpath;
		}
	}
}
