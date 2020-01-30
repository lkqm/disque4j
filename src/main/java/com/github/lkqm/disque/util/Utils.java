package com.github.lkqm.disque.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.github.lkqm.disque.Message;
import com.github.lkqm.disque.MessageListenerInfo;
import com.github.lkqm.disque.spring.ThreadPoolConfig;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {

    private static final SnowFlake snowFlake = new SnowFlake(10, 1);

    private static volatile String ipCache;

    /**
     * 检查消息是否可以被改处理器消费
     *
     * @param message
     * @param listenerInfo
     * @return
     */
    public static boolean checkMatchMessageListener(Message message, MessageListenerInfo listenerInfo) {
        String tag = message.getTag() == null ? "" : message.getTag().trim();
        String[] targetTags = listenerInfo.getTags();
        if (targetTags == null || targetTags.length == 0) return true;

        for (String targetTag : targetTags) {
            if (tag.equals(targetTag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 创建线程池
     *
     * @param config
     * @return
     */
    public static ExecutorService createThreadPool(String namePrefix, ThreadPoolConfig config) {
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue(config.getQueueCapacity());
        return new ThreadPoolExecutor(config.getCoreSize(), config.getMaxSize(), config.getKeepAlive(), TimeUnit.MILLISECONDS, workQueue, new ThreadFactory() {

            AtomicInteger counter = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(namePrefix + counter.incrementAndGet());
                return thread;
            }
        }, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 填充默认线程池配置
     *
     * @param config
     */
    public static ThreadPoolConfig fillDefaultThreadPoolConfig(ThreadPoolConfig config) {
        ThreadPoolConfig result = copyThreadPoolConfig(config);
        Integer corePoolSize = result.getCoreSize();
        if (corePoolSize == null || corePoolSize <= 0) {
            result.setCoreSize(1);
        }
        Integer maxPoolSize = result.getMaxSize();
        if (maxPoolSize == null || maxPoolSize <= 0) {
            result.setMaxSize(result.getCoreSize());
        }
        Long keepAliveTime = result.getKeepAlive();
        if (keepAliveTime == null || keepAliveTime <= 0) {
            result.setKeepAlive(100L);
        }
        if (result.getQueueCapacity() == null || result.getQueueCapacity() <= 0) {
            result.setQueueCapacity(result.getMaxSize());
        }
        return result;
    }

    /**
     * 合并线程池配置
     */
    public static ThreadPoolConfig mergeAndFillThreadPoolConfig(ThreadPoolConfig high, ThreadPoolConfig low) {
        ThreadPoolConfig result = copyThreadPoolConfig(high);

        Integer corePoolSize = result.getCoreSize();
        if (corePoolSize == null || corePoolSize <= 0) {
            result.setCoreSize(low.getCoreSize());
        }
        Integer maxPoolSize = result.getMaxSize();
        if (maxPoolSize == null || maxPoolSize <= 0) {
            result.setMaxSize(low.getMaxSize());
        }
        Long keepAliveTime = result.getKeepAlive();
        if (keepAliveTime == null || keepAliveTime <= 0) {
            result.setKeepAlive(low.getKeepAlive());
        }
        if (result.getQueueCapacity() == null || result.getQueueCapacity() <= 0) {
            result.setQueueCapacity(low.getQueueCapacity());
        }
        result = fillDefaultThreadPoolConfig(result);
        return result;
    }

    private static ThreadPoolConfig copyThreadPoolConfig(ThreadPoolConfig config) {
        ThreadPoolConfig result = new ThreadPoolConfig();
        result.setCoreSize(config.getCoreSize());
        result.setMaxSize(config.getMaxSize());
        result.setKeepAlive(config.getKeepAlive());
        result.setQueueCapacity(config.getQueueCapacity());
        return result;
    }

    /**
     * 生产消息ID
     *
     * @return
     */
    public static String generateMessageId(String topic) {
        String ip = ip();
        StringBuilder sb = new StringBuilder().append(topic.toUpperCase());
        sb.append("-").append(ip);
        sb.append("-").append(snowFlake.nextId());

        return sb.toString();
    }

    public static String messageToJson(Message message) {
        return JSON.toJSONString(message);
    }

    public static Message jsonToMessage(String data) throws JSONException {
        return JSON.parseObject(data, Message.class);
    }


    public static void sleepSilence(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            // eat some thing
        }
    }

    private static String ip() {
        if (ipCache != null) return ipCache;

        try {
            ipCache = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            ipCache = "127.0.0.1";
        }
        return ipCache;
    }
}