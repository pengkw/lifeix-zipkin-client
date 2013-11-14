lifeix-zipkin-client
====================

java使用zipkin，这个client主要基于https://github.com/kristofa/brave 作出小的改动，确保调用zipkin服务功能不会受到影响。

1.修改ZipkinSpanCollector中collect方法，队列满后直接丢弃，不再阻塞等待。
2.修改ZooKeeperSamplingTraceFilter，增加连接zookeeper等待超时时间；增加sampleRate为double处理。
3.对调用做了简单的封装，保证调用服务在zipkin相关服务出现问题时不影响调用服务的正常功能。

打包：
	mvn clean package

调用：

    	//Init zipkin
	ZipKinConfig zkConfig = new ZipKinConfig();
	zkConfig.setTraceServerIp("127.0.0.1");
	zkConfig.setTraceServerPort(8080);
	zkConfig.setTraceServerName("ServiceName");
	zkConfig.setCollectorServerIp("127.0.0.1");
	zkConfig.setCollectorServerPort(9410);
	zkConfig.setSpanQueueSize(150);
	zkConfig.setZkServerConnectString("127.0.0.1:2181");
	zkConfig.setSampleRateNode("/zipkin/config/samplerat");
	ZipkinCollectorClient.getInstance().init(zkConfig);
	
	//Begin tracer
	ZipkinCollectorClient tracer = ZipkinCollectorClient.getInstance();
	tracer.startNewSpan("timeline");
	tracer.setClientSent();
	
	//Your code here
	tracer.setClientReceived();
	
	//Shutdown
	ZipkinCollectorClient.getInstance().shutdown();
