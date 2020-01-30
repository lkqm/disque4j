package com.github.lkqm.disque;

import lombok.Data;

import java.io.Serializable;

/**
 * 消费者绑定相关信息
 */
@Data
public class MessageListenerInfo implements Serializable {

    /**
     * 队列
     */
    private String topic;

    /**
     * 标签
     */
    private String[] tags;

    /**
     * 处理类
     */
    private MessageListener listener;

    public static MessageListenerInfo of(String topic, String[] tags, MessageListener listener) {
        MessageListenerInfo info = new MessageListenerInfo();
        info.topic = topic;
        info.tags = tags;
        info.listener = listener;
        return info;
    }

}