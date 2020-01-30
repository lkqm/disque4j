package com.github.lkqm.disque;

import lombok.experimental.UtilityClass;

/**
 * 消息发送工具类
 */
@UtilityClass
public class MessageProducers {

    private static volatile MessageProducer producer;

    /**
     * 初始化(必须执行一次)
     */
    public static void init(MessageProducer target) {
        producer = target;
    }

    /**
     * 发送消息
     *
     * @param topic 队列
     * @param tag   消息标记
     * @param data  数据
     * @return
     */
    public static String publish(String topic, String tag, Object data) {
        return getProducer().publish(topic, tag, data);
    }

    /**
     * 发送消息(不会抛出底层异常)
     *
     * @param topic 队列
     * @param tag   消息标记
     * @param data  数据
     * @return
     */
    public static String publishSilence(String topic, String tag, Object data) {
        return getProducer().publishSilence(topic, tag, data);
    }

    private static MessageProducer getProducer() {
        if (producer == null) {
            throw new RuntimeException("未初始化, 请执行: MessageProducers.init(producer)");
        }
        return producer;
    }
}