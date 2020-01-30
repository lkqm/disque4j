package com.github.lkqm.disque.spring;

import com.github.lkqm.disque.*;
import com.github.lkqm.disque.ops.JedisRedisOps;
import com.github.lkqm.disque.ops.RedisTemplateRedisOps;
import com.github.lkqm.disque.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * 消息队列配置
 */
@Configuration
@EnableConfigurationProperties(DisqueProperties.class)
@ConditionalOnProperty(prefix = DisqueProperties.PREFIX, name = "enabled", havingValue = "true")
@Slf4j
public class DisqueAutoConfiguration {

    @Autowired
    private DisqueProperties disqueProperties;

    @Autowired
    private ApplicationContext ctx;

    @Bean("jedisRedisOpsDisque")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = DisqueProperties.PREFIX, name = "redis.host")
    public RedisOps jedisRedisOps() {
        return new JedisRedisOps(disqueProperties.getKeyPrefix(), createJedisPool());
    }

    @Bean("redisTemplateRedisOpsDisque")
    @ConditionalOnMissingBean
    public RedisOps redisTemplateRedisOps(StringRedisTemplate redisTemplate) {
        return new RedisTemplateRedisOps(disqueProperties.getKeyPrefix(), redisTemplate);
    }

    @Bean("messageProducerDisque")
    public MessageProducer messageProducer(RedisOps redisOps) {
        return new MessageProducer(redisOps);
    }

    @Bean
    public Object initMessageProducers(MessageProducer messageProducer) {
        MessageProducers.init(messageProducer);
        return Boolean.TRUE;
    }

    @Bean("messageReceiverManagerDisque")
    public MessageReceiverManager messageReceiverManager(RedisOps redisOps) {
        log.info("Disque enabled: keyPrefix={}", disqueProperties.getKeyPrefix());
        ExecutorService pool = getDisqueThreadPool();
        Map<String, ExecutorService> topicPool = getDisqueTopicPool();
        MessageReceiverManager manager = new MessageReceiverManager(redisOps, pool, topicPool);
        List<MessageListenerInfo> listeners = getMessageListenerInfos();
        manager.addAllListener(listeners);
        manager.start();
        return manager;
    }

    // 获取全局消费者线程池
    private ExecutorService getDisqueThreadPool() {
        ThreadPoolConfig threadPoolConfig = Utils.fillDefaultThreadPoolConfig(disqueProperties.getPool());
        return Utils.createThreadPool(DisqueProperties.POOL_NAME_PREFIX, threadPoolConfig);
    }

    // 获取指定队列的消费者线程池
    private Map<String, ExecutorService> getDisqueTopicPool() {
        Map<String, ExecutorService> pools = new HashMap<>();

        Map<String, DisqueProperties.TopicConfig> topics = disqueProperties.getTopics();
        if (topics == null) return null;

        Iterator<Map.Entry<String, DisqueProperties.TopicConfig>> it = topics.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, DisqueProperties.TopicConfig> next = it.next();
            String topic = next.getKey();
            DisqueProperties.TopicConfig topicConfig = next.getValue();
            ThreadPoolConfig topicThreadPoolConfig = topicConfig.getPool();
            if (topicThreadPoolConfig != null) {
                topicThreadPoolConfig = Utils.mergeAndFillThreadPoolConfig(topicThreadPoolConfig, disqueProperties.getPool());
                ExecutorService pool = Utils.createThreadPool(DisqueProperties.POOL_NAME_PREFIX + topic + "-", topicThreadPoolConfig);
                pools.put(topic, pool);
            }
        }
        return pools;
    }

    // 获得消息监听者信息
    private List<MessageListenerInfo> getMessageListenerInfos() {
        Collection<Object> handlers = ctx.getBeansWithAnnotation(RedisListener.class).values();
        List<MessageListenerInfo> listeners = new ArrayList<>(handlers.size());
        for (Object handler : handlers) {
            if (handler instanceof MessageListener) {
                MessageListener listener = (MessageListener) handler;
                RedisListener annotation = AnnotationUtils.getAnnotation(listener.getClass(), RedisListener.class);
                MessageListenerInfo listenerInfo = MessageListenerInfo.of(annotation.topic(), annotation.tags(), listener);
                listeners.add(listenerInfo);
            }
        }
        return listeners;
    }

    // 创建连接池
    private JedisPool createJedisPool() {

        RedisConfig redis = disqueProperties.getRedis();

        JedisPoolConfig config = new JedisPoolConfig();
        if (redis.getMaxActive() != null) {
            config.setMaxTotal(redis.getMaxActive());
        }
        if (redis.getMaxIdle() != null) {
            config.setMaxIdle(redis.getMaxIdle());
        }
        if (redis.getMaxWaitMillis() != null) {
            config.setMaxWaitMillis(redis.getMaxWaitMillis());
        }
        if (redis.getMinIdle() != null) {
            config.setMinIdle(redis.getMinIdle());
        }
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(true);

        JedisPool pool = new JedisPool(config, redis.getHost(), redis.getPort(),
                redis.getTimeout(), redis.getPassword(), redis.getDatabase());
        return pool;
    }
}
