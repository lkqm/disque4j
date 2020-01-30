package com.github.lkqm.disque;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息
 */
@Data
public class Message implements Serializable {

    /**
     * 消息ID
     */
    private String id;

    /**
     * 队列
     */
    private String topic;

    /**
     * 消息标签
     */
    private String tag;

    /**
     * 数据
     */
    private String data;

    /**
     * 消息时间
     */
    private Long timestamp;
}
