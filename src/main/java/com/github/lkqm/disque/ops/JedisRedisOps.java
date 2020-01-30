package com.github.lkqm.disque.ops;

import com.github.lkqm.disque.RedisOps;
import lombok.AllArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

/**
 * 基于Jedis进行的数据操作
 */
@AllArgsConstructor
public class JedisRedisOps implements RedisOps {

    private String keyPrefix;
    private JedisPool jedisPool;

    @Override
    public void publishMessage(String topic, String message) {
        Jedis jedis = jedisPool.getResource();
        try {
            String key = topicKey(topic);
            jedis.lpush(key, message);
        } finally {
            jedis.close();
        }
    }

    @Override
    public String popMessage(String topic) {
        Jedis jedis = jedisPool.getResource();
        try {
            String data = null;
            String key = topicKey(topic);
            List<String> list = jedis.brpop(5, key);
            if (list != null && list.size() > 0) {
                data = list.get(1);
            }
            return data;
        } finally {
            jedis.close();
        }
    }

    // 消息队列key
    private String topicKey(String topic) {
        return (keyPrefix == null || keyPrefix.length() == 0) ? topic : keyPrefix + ":" + topic;
    }
}
