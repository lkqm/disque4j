package com.github.lkqm.disque;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 消费者注解
 */
@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisListener {

    /**
     * 队列（不包含前缀)
     */
    String topic();

    /**
     * 消息标签
     */
    String[] tags() default {};
}