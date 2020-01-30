package com.github.lkqm.disque.ops;

import com.github.lkqm.disque.RedisOps;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 基于RedisTemplate进行的数据操作
 */
@AllArgsConstructor
public class RedisTemplateRedisOps implements RedisOps {

    private String keyPrefix;
    private StringRedisTemplate redisTemplate;

    @Override
    public void publishMessage(String topic, String message) {
        String key = topicKey(topic);
        this.redisTemplate.opsForList().leftPush(key, message);
    }

    @Override
    public String popMessage(String topic) {
        String key = topicKey(topic);
        return this.redisTemplate.opsForList().rightPop(key, 5, TimeUnit.SECONDS);
    }

    // 消息队列key
    private String topicKey(String topic) {
        return (keyPrefix == null || keyPrefix.length() == 0) ? topic : keyPrefix + ":" + topic;
    }
}
