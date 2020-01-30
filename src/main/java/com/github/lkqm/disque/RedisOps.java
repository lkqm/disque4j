package com.github.lkqm.disque;

/**
 * Redis操作
 */
public interface RedisOps {

    /**
     * 发布消息到指定队列
     *
     * @param topic
     * @param message
     */
    void publishMessage(String topic, String message);

    /**
     * 获取消息列表
     *
     * @param topic
     * @return
     */
    String popMessage(String topic);

}