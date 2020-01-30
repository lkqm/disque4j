package com.github.lkqm.disque;

/**
 * 消息接收者
 */
public interface MessageReceiver {

    /**
     * 启动
     */
    void start();

    /**
     * 停止
     */
    void stop();

    /**
     * 添加监听器
     */
    void addListener(MessageListenerInfo listenerInfo);
}