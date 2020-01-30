# disque4j
A simple redis message queue for Java.

一个易于使用的基于redis的消息队列, 不支持集群消费.

## 快速开始
1. 添加依赖
    ```xml
    <dependency>
        <groupId>com.github.lkqm</groupId>
        <artifactId>disque4j</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
    ```

2. 生产者
   ```
   MessageProducers.publish("order", "add", "Add one order.");
   ```

3. 消费者
    ```java
    @RedisListener(topic="order", tags="add")
    public class OrderConsumer implements MessageListener {
        
       @Override
        public void onMessage(Message message) {
            System.out.println("receive message: " + message);
        }
    }
    ```

4. 配置
    ```properties
   disque.enabled=true                               # 开启, 默认: false
   disque.key-prefix=disque                          # 设置key前置, 默认: disque
   disque.pool.core-size=1                           # 核心线程数量, 默认: 1
   disque.pool.max-size=8                            # 最大线程数量, 默认: 8
   disque.topics.YOUR_TOPIC.pool.core-size=1         # 配置指定队列的线程池
   disque.topics.YOUR_TOPIC.pool.max-size=8          # 配置指定队列的线程池
   disque.topics.YOUR_TOPIC.pool.keep-alive=         # 配置指定队列的线程池
   disque.topics.YOUR_TOPIC.pool.queue-capacity=     # 配置指定队列的线程池
    ```

## 警告
请确保redis连接超时 > 5秒(bpop阻塞时间), 否则会出现消息丢失, 默认redis连接不会超时。

