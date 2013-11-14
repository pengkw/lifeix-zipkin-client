package com.lifeix.zipkin;


public class ZipKinConfig  {
	
	/**
	 * false 表示通过zookeeper获取sampleRateNode上值进行初始化连接，以及对该值的变化作出及时的响应。可以在不是重启应用的情况下来实现开启或关闭追踪。
	 * true 表示直接使用使用sampleRate值连接zipkin collector service
	 */
	private boolean skipZk = false;
	
	/**
	 * 追踪服务器的ip
	 */
	private String traceServerIp;
	
	/**
	 * 追踪服务器的端口
	 */
	private Integer traceServerPort = 8080;
	
	/**
	 * 追踪服务器上服务的名称（例如L06、lifeix-dovebox）
	 */
	private String traceServerName;
	
	/**
	 * zipkin collector server ip地址
	 */
	private String collectorServerIp;
	
	/**
	 * zipkin collector server 监听的端口
	 */
	private Integer collectorServerPort;
	
	/**
	 * 收集span的队列大小 
	 */
	private Integer spanQueueSize = 100;
	
	/**
	 * zookeeper服务器连接字符串（例如：192.168.1.100：2181,192.168.1.101:2181,192.168.1.103:2181）
	 */
	private String zkServerConnectString;
	
	/**
	 * zookeeper上存储是否开启zipkin追踪的node
	 * 在该节点上存储追踪的rate值，该值的大小含义参见下面rateNode的说明
	 */
	private String sampleRateNode = "/lifeix/zipkin/config/samplerate";
	
	/**
	 * 当zookeeper连接出现问题的时候，会使用下面rate值，跳过zookeeper直接连接zipkin collector server
	 * 0 表示关闭追踪
	 * 1 表示对所有的请求追踪（不建议将此值设置为1，访问量大的情况下会对性能有很大的影响）
	 * >1 表示没多少个请求，追踪一次。例如，5表示每5个请求追踪一次
	 * 在生产环境中如果开启追踪，建议将rate值设置为大于1的数字。具体数值可以根据应用的访问量和spanQueueSize的大小来决定。
	 */
	private Integer sampleRate = 0;
    
	public ZipKinConfig(Boolean skipZk, String traceServerIp, Integer traceServerPort,
			String traceServerName, String collectorServerIp,
			Integer collectorServerPort, String zkServerConnectString,
			Integer sampleRate, String sampleRateNode) {
		super();
		this.skipZk = skipZk;
		this.traceServerIp = traceServerIp;
		this.traceServerPort = traceServerPort;
		this.traceServerName = traceServerName;
		this.collectorServerIp = collectorServerIp;
		this.collectorServerPort = collectorServerPort;
		this.zkServerConnectString = zkServerConnectString;
		this.sampleRate = sampleRate;
		this.sampleRateNode = sampleRateNode;
	}

	public ZipKinConfig() {
		super();
	}
	
	public boolean isSkipZk() {
		return skipZk;
	}

	public void setSkipZk(boolean skipZk) {
		this.skipZk = skipZk;
	}
	
	public String getTraceServerIp() {
		return traceServerIp;
	}

	public void setTraceServerIp(String traceServerIp) {
		this.traceServerIp = traceServerIp;
	}

	public Integer getTraceServerPort() {
		return traceServerPort;
	}

	public void setTraceServerPort(Integer traceServerPort) {
		this.traceServerPort = traceServerPort;
	}

	public String getTraceServerName() {
		return traceServerName;
	}

	public void setTraceServerName(String traceServerName) {
		this.traceServerName = traceServerName;
	}

	public String getCollectorServerIp() {
		return collectorServerIp;
	}

	public void setCollectorServerIp(String collectorServerIp) {
		this.collectorServerIp = collectorServerIp;
	}

	public Integer getCollectorServerPort() {
		return collectorServerPort;
	}

	public void setCollectorServerPort(Integer collectorServerPort) {
		this.collectorServerPort = collectorServerPort;
	}

	public String getZkServerConnectString() {
		return zkServerConnectString;
	}

	public void setZkServerConnectString(String zkServerConnectString) {
		this.zkServerConnectString = zkServerConnectString;
	}

	public Integer getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(Integer sampleRate) {
		this.sampleRate = sampleRate;
	}

	public String getSampleRateNode() {
		return sampleRateNode;
	}

	public void setSampleRateNode(String sampleRateNode) {
		this.sampleRateNode = sampleRateNode;
	}

	public Integer getSpanQueueSize() {
		return spanQueueSize;
	}

	public void setSpanQueueSize(Integer spanQueueSize) {
		this.spanQueueSize = spanQueueSize;
	}
 
}

