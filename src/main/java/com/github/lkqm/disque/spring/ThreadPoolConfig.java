package com.github.lkqm.disque.spring;

import lombok.Data;

import java.io.Serializable;

/**
 * 线程池配置
 */
@Data
public class ThreadPoolConfig implements Serializable {

    /**
     * 核心线程大小
     */
    private Integer coreSize = 1;

    /**
     * 最大线程大小
     */
    private Integer maxSize = 8;

    /**
     * 空闲线程存活时间
     */
    private Long keepAlive;

    /**
     * 工作队列大小
     */
    private Integer queueCapacity;

}
