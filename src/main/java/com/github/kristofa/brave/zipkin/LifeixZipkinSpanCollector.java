package com.github.kristofa.brave.zipkin;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PreDestroy;

import org.apache.commons.lang.Validate;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kristofa.brave.SpanCollector;
import com.twitter.zipkin.gen.AnnotationType;
import com.twitter.zipkin.gen.BinaryAnnotation;
import com.twitter.zipkin.gen.Span;


/**
 * 重写此类，主要解决向队列中加入任务等待问题
 * @author pengkw
 */
public class LifeixZipkinSpanCollector implements SpanCollector {

    private static final String UTF_8 = "UTF-8";
    private static final int DEFAULT_MAX_QUEUESIZE = 100;
    private static final Logger LOGGER = LoggerFactory.getLogger(LifeixZipkinSpanCollector.class);

    private final ZipkinCollectorClientProvider clientProvider;
    private final BlockingQueue<Span> spanQueue;
    private final ExecutorService executorService;
    private final SpanProcessingThread spanProcessingThread;
    private final Future<Integer> future;
    private final Set<BinaryAnnotation> defaultAnnotations = new HashSet<BinaryAnnotation>();

    /**
     * Create a new instance with default queue size (=50).
     * 
     * @param zipkinCollectorHost Host for zipkin collector.
     * @param zipkinCollectorPort Port for zipkin collector.
     */
    public LifeixZipkinSpanCollector(final String zipkinCollectorHost, final int zipkinCollectorPort) {
        this(zipkinCollectorHost, zipkinCollectorPort, DEFAULT_MAX_QUEUESIZE);
    }

    /**
     * Create a new instance.
     * 
     * @param zipkinCollectorHost Host for zipkin collector.
     * @param zipkinCollectorPort Port for zipkin collector.
     * @param maxQueueSize Maximum queue size.
     */
    public LifeixZipkinSpanCollector(final String zipkinCollectorHost, final int zipkinCollectorPort, final int maxQueueSize) {
        Validate.notEmpty(zipkinCollectorHost);
        clientProvider = new ZipkinCollectorClientProvider(zipkinCollectorHost, zipkinCollectorPort);
        try {
            clientProvider.setup();
        } catch (final TException e) {
            throw new IllegalStateException(e);
        }
        spanQueue = new ArrayBlockingQueue<Span>(maxQueueSize);
        spanProcessingThread = new SpanProcessingThread(spanQueue, clientProvider);
        executorService = Executors.newSingleThreadExecutor();
        future = executorService.submit(spanProcessingThread);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void collect(final Span span) {
        try {
            if (!defaultAnnotations.isEmpty()) {
                for (final BinaryAnnotation ba : defaultAnnotations) {
                    span.addToBinary_annotations(ba);
                }
            }
           if(!spanQueue.offer(span)){
        	   LOGGER.info("Unable to submit span to queue: " + span);
           }
          
        } catch (Exception e) {
            LOGGER.error("Unable to submit span to queue: " + span + " " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDefaultAnnotation(final String key, final String value) {
        Validate.notEmpty(key);
        Validate.notNull(value);

        try {
            final ByteBuffer bb = ByteBuffer.wrap(value.getBytes(UTF_8));

            final BinaryAnnotation binaryAnnotation = new BinaryAnnotation();
            binaryAnnotation.setKey(key);
            binaryAnnotation.setValue(bb);
            binaryAnnotation.setAnnotation_type(AnnotationType.STRING);
            defaultAnnotations.add(binaryAnnotation);

        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreDestroy
    public void close() {

        LOGGER.info("Stopping SpanProcessingThread.");
        spanProcessingThread.stop();
        try {
            final Integer spansProcessed = future.get();
            LOGGER.info("SpanProcessingThread processed " + spansProcessed + " spans.");
        } catch (final Exception e) {
            LOGGER.error("Exception when getting result of SpanProcessingThread.", e);
        }
        executorService.shutdown();
        clientProvider.close();
        LOGGER.info("ZipkinSpanCollector closed.");
    }

}
