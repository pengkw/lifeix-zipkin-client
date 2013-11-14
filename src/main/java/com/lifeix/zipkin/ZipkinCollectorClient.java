package com.lifeix.zipkin;

import java.net.InetAddress;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ClientTracer;
import com.github.kristofa.brave.EndPointSubmitter;
import com.github.kristofa.brave.FixedSampleRateTraceFilter;
import com.github.kristofa.brave.SpanCollector;
import com.github.kristofa.brave.SpanId;
import com.github.kristofa.brave.TraceFilter;
import com.github.kristofa.brave.zipkin.LifeixZipkinSpanCollector;

public class ZipkinCollectorClient {

	private final Logger LOG = LoggerFactory.getLogger(ZipkinCollectorClient.class);

	private SpanCollector spanCollector;

	private TraceFilters traceFilters;

	private ClientTracer tracer;

	private TraceFilter traceFilter;

	// private final static String SAMPLE_RATE_NODE = "/twitter/service/zipkin/config/samplerate";
	//private final static String SAMPLE_RATE_NODE = "/lifeix/zipkin/config/samplerate";

	private ZipkinCollectorClient() {
		
	}

	private static class InstanceHolder {
		private static final ZipkinCollectorClient INSTANCE = new ZipkinCollectorClient();
	}

	public static ZipkinCollectorClient getInstance() {
		return InstanceHolder.INSTANCE;
	}
	
	public void init(ZipKinConfig config) {
		// The trace server address and name
		final EndPointSubmitter endPointSubmitter = Brave.getEndPointSubmitter();
		
		if(config.getTraceServerIp()==null||config.getTraceServerIp().equals("")){
			InetAddress addr;
			try {
				addr = InetAddress.getLocalHost();
				config.setTraceServerIp(addr.getHostAddress());
			} catch (Exception e) {
				config.setTraceServerIp("127.0.0.1");
			}			
		}
		endPointSubmitter.submit(config.getTraceServerIp(), config.getTraceServerPort(), config.getTraceServerName());
		
		//	The zipkin-collector-server ip and port
		//this.spanCollector = new ZipkinSpanCollector(config.getCollectorServerIp(), config.getCollectorServerPort());
		this.spanCollector = new LifeixZipkinSpanCollector(config.getCollectorServerIp(), config.getCollectorServerPort(), config.getSpanQueueSize());
		
		if(config.isSkipZk()){
			/*
			 * If sample rate value <= 0 tracing is disabled. In that case we won't trace any request.
			 * If sample rate value = 1 we will trace every request.
			 * If sample rate value > 1, for example 5, we will trace every 5 requests.
			 */
			int rate = config.getSampleRate();
			this.traceFilter = new FixedSampleRateTraceFilter(rate);
			LOG.info("===============Skip zookeeper server. Use FixedSampleRateTraceFilter with rate[" + rate + "]==============");
		}else{
			try {
				// The zookeeper server ip and port
				this.traceFilter = new ZooKeeperSamplingTraceFilter(config.getZkServerConnectString(), config.getSampleRateNode());
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
		if(traceFilter != null){
			this.traceFilters = new TraceFilters(Arrays.<TraceFilter> asList(traceFilter));
			this.tracer = Brave.getClientTracer(spanCollector, traceFilters.getTraceFilters());
		}
	}

	/**
	 * 
	 * @return
	 */
	public ClientTracer getClientTracer() {
		return tracer;
	}
	
	public ZipkinCollectorClient getClient(){
		return this;
	}
	
    /**
     * Start a new span for a new client request that will be bound to current thread. The ClientTracer can decide to return
     * <code>null</code> in case this request should not be traced (eg sampling).
     * 
     * @param requestName Request name. Should not be <code>null</code> or empty.
     * @return Span id for new request or <code>null</code> in case we should not trace this new client request.
     */
	public SpanId startNewSpan(final String requestName){
		if(tracer == null)
			return null;
		return tracer.startNewSpan(requestName);
	}
	
	 /**
     * Sets 'client sent' event for current thread.
     */
    public void setClientSent(){
    	if(tracer != null)
    		tracer.setClientSent();
    }

    /**
     * Sets the 'client received' event for current thread. This will also submit span because setting a client received
     * event means this span is finished.
     */
    public void setClientReceived(){
    	if(tracer != null)
    		tracer.setClientReceived();
    }
	
    /**
     * Submits custom annotation that represents an event with duration.
     * 
     * @param annotationName Custom annotation.
     * @param startTime Start time, <a href="http://en.wikipedia.org/wiki/Unix_time">Unix time</a> in milliseconds. eg
     *            System.currentTimeMillis().
     * @param endTime End time, Unix time in milliseconds. eg System.currentTimeMillis().
     */
    public void submitAnnotation(final String annotationName, final long startTime, final long endTime){
    	if(tracer != null)
    		tracer.submitAnnotation(annotationName, startTime, endTime);
    }
    
    /**
     * Submits custom annotation for current span. Use this method if your annotation has no duration assigned to it.
     * 
     * @param annotationName Custom annotation for current span.
     */
    public void submitAnnotation(final String annotationName){
    	if(tracer != null)
    		tracer.submitAnnotation(annotationName);
    }
    
    /**
     * Submits a binary (key/value) annotation with String value.
     * 
     * @param key Key, should not be blank.
     * @param value String value, should not be <code>null</code>.
     */
    public void submitBinaryAnnotation(final String key, final String value){
    	if(tracer != null)
    		tracer.submitBinaryAnnotation(key, value);
    }

    /**
     * Submits a binary (key/value) annotation with int value.
     * 
     * @param key Key, should not be blank.
     * @param value Integer value.
     */
    public void submitBinaryAnnotation(final String key, final int value){
    	if(tracer != null)
    		tracer.submitBinaryAnnotation(key, value);
    }
    
	/**
	 * close connections
	 */
	public void shutdown() {
		if (spanCollector != null)
			spanCollector.close();
		if(traceFilter != null)
			traceFilter.close();
	}
}
