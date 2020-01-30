package com.github.lkqm.disque;

/**
 * 消费者接口
 */
public interface MessageListener {

    void onMessage(Message message) throws Exception;

}
