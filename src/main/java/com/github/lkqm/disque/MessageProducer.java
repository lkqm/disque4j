package com.github.lkqm.disque;

import com.alibaba.fastjson.JSON;
import com.github.lkqm.disque.util.Utils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息发送者接口
 */
@AllArgsConstructor
@Slf4j
public class MessageProducer {

    private RedisOps redisOps;

    /**
     * 发送消息
     *
     * @param topic 队列
     * @param tag   标记
     * @param data  数据
     * @return
     */
    String publish(String topic, String tag, Object data) {
        Message message = getMessage(topic, tag, data);
        String text = Utils.messageToJson(message);
        redisOps.publishMessage(topic, text);
        log.debug("Send message: {}", text);
        return message.getId();
    }

    /**
     * 发送消息(不抛出异常)
     *
     * @param topic 队列
     * @param tag   标记
     * @param data  数据
     * @return 发送消息时返回null值
     */
    public String publishSilence(String topic, String tag, Object data) {
        Message message = getMessage(topic, tag, data);
        String text = Utils.messageToJson(message);
        try {
            redisOps.publishMessage(topic, text);
            log.debug("Send message: {}", text);
            return message.getId();
        } catch (Exception e) {
            log.error("Publish message failed: {}", text, e);
            return null;
        }
    }

    private Message getMessage(String topic, String tag, Object data) {
        Message message = new Message();
        message.setId(Utils.generateMessageId(topic));
        message.setTopic(topic);
        message.setTag(tag);
        message.setTimestamp(System.currentTimeMillis());
        message.setData(JSON.toJSONString(data));
        return message;
    }
}