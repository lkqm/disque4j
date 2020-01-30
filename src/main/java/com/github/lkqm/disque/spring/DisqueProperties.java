package com.github.lkqm.disque.spring;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;
import java.util.Map;

import static com.github.lkqm.disque.spring.DisqueProperties.PREFIX;

/**
 * 配置类
 */
@Data
@ConfigurationProperties(PREFIX)
public class DisqueProperties implements Serializable {

    public static final String PREFIX = "disque";
    public static final String POOL_NAME_PREFIX = "disque-";

    /**
     * 是否开启
     */
    private boolean enabled = false;

    /**
     * 前缀
     */
    private String keyPrefix = "disque";

    /**
     * 全局线程池配置
     */
    private ThreadPoolConfig pool = new ThreadPoolConfig();

    /**
     * reids配置
     */
    private RedisConfig redis;

    /**
     * 队列设置
     */
    private Map<String, TopicConfig> topics;

    /**
     * 队列指定配置
     */
    @Data
    public static class TopicConfig implements Serializable {

        /**
         * 指定队列消费线程配置
         */
        private ThreadPoolConfig pool;
    }

}
