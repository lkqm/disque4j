package com.github.lkqm.disque;

import com.github.lkqm.disque.util.Utils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 抽象消息监听特定队列
 */
@Getter
@Slf4j
public class MessageReceiverImpl implements MessageReceiver {

    /**
     * 监听的队列
     */
    private String topic;

    /**
     * 相关redis操作
     */
    private RedisOps redisOps;

    /**
     * 消费线程池
     */
    private ExecutorService pool;

    protected List<MessageListenerInfo> listeners = new ArrayList<>();

    private AtomicBoolean status = new AtomicBoolean();
    private Thread loopReceiveThread;

    public MessageReceiverImpl(String topic, RedisOps redisOps, ExecutorService pool) {
        this.topic = topic;
        this.redisOps = redisOps;
        this.pool = pool;
    }

    @Override
    public void start() {
        if (status.compareAndSet(false, true)) {
            this.loopReceiveThread = new Thread(() -> {
                loopReceive();
            });
            this.loopReceiveThread.setName("disque-receiver-" + topic);
            this.loopReceiveThread.start();
            if (log.isInfoEnabled()) {
                log.info("Start message receiver for topic={}, listeners=[{}]", topic, listeners);
            }
        }
    }

    @Override
    public void stop() {
        if (status.compareAndSet(true, false)) {
            this.loopReceiveThread.interrupt();
            log.info("Stop message receiver for topic: {}", topic);
        }
    }

    @Override
    public void addListener(MessageListenerInfo listenerInfo) {
        listeners.add(listenerInfo);
    }

    /**
     * 循环获取数据
     */
    private void loopReceive() {
        while (status.get()) {
            try {
                String data = this.redisOps.popMessage(topic);
                if (data != null) {
                    log.debug("Receive message: {}", data);
                    Message message = convertMessage(data);
                    if (message != null) {
                        dispatchMessage(message);
                    }
                }
            } catch (Exception e) {
                Utils.sleepSilence(1 * 1000);
            }
        }
    }

    /**
     * 分发消费消息
     */
    private void dispatchMessage(Message message) {
        for (MessageListenerInfo listenerInfo : listeners) {
            if (listenerInfo == null || !Utils.checkMatchMessageListener(message, listenerInfo)) {
                log.debug("Dispatch message not find match listeners: {}", message);
                continue;
            }
            pool.submit(() -> {
                MessageListener handler = listenerInfo.getListener();
                try {
                    handler.onMessage(message);
                } catch (Exception e) {
                    String listenerName = listenerInfo.getListener().getClass().getName();
                    log.error("Consumer message exception from listener[{}]: {}", listenerName, message, e);
                }
            });
            if (log.isDebugEnabled()) {
                String listenerName = listenerInfo.getListener().getClass().getName();
                log.debug("Dispatch message to listener[{}]: {}", listenerName, message);
            }
        }
    }

    /**
     * 消息格式转换
     */
    private Message convertMessage(String data) {
        if (data == null) return null;
        try {
            return Utils.jsonToMessage(data);
        } catch (Exception e) {
            return null;
        }
    }
}