package com.github.lkqm.disque;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * 消息接受者管理器(抽象)
 */
public class MessageReceiverManager implements MessageReceiver {

    /**
     * 相关redis操作
     */
    private RedisOps redisOps;

    /**
     * 消费者进程执行线程池
     */
    private ExecutorService pool;

    /**
     * 指定队列的消费者线程
     */
    private Map<String, ExecutorService> topicPoolMap;

    /**
     * 管理的消息接受者
     */
    private Map<String, MessageReceiver> messageReceivers = new ConcurrentHashMap<>();


    public MessageReceiverManager(RedisOps redisOps, ExecutorService pool) {
        this(redisOps, pool, null);
    }

    public MessageReceiverManager(RedisOps redisOps, ExecutorService pool, Map<String, ExecutorService> topicPoolMap) {
        this.redisOps = redisOps;
        this.pool = pool;
        this.topicPoolMap = topicPoolMap;
    }


    @Override
    public void start() {
        for (MessageReceiver receiver : messageReceivers.values()) {
            receiver.start();
        }
    }

    @Override
    public void stop() {
        for (MessageReceiver receiver : messageReceivers.values()) {
            receiver.stop();
        }
    }

    @Override
    public void addListener(MessageListenerInfo listenerInfo) {
        String topic = listenerInfo.getTopic();
        MessageReceiver receiver = messageReceivers.get(topic);
        if (receiver == null) {
            ExecutorService topicPool = getTopicThreadPool(topic);
            receiver = new MessageReceiverImpl(topic, redisOps, topicPool);
            messageReceivers.put(topic, receiver);
        }
        receiver.addListener(listenerInfo);
    }

    public void addAllListener(List<MessageListenerInfo> listenerInfos) {
        for (MessageListenerInfo listenerInfo : listenerInfos) {
            addListener(listenerInfo);
        }
    }

    private ExecutorService getTopicThreadPool(String topic) {
        ExecutorService pool = null;
        if (topicPoolMap != null) {
            pool = topicPoolMap.get(topic);
        }
        return pool != null ? pool : this.pool;
    }

}
