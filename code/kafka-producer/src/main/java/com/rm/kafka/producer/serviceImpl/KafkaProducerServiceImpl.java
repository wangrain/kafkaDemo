package com.rm.kafka.producer.serviceImpl;

import com.rm.kafka.producer.service.KafkaProducerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Properties;

@Slf4j
@Service
public class KafkaProducerServiceImpl implements KafkaProducerService {
    @Value("${kafka.bootstrap.servers}")
    private String serverUrl;
    @Value("${kafka.request.timeout.ms}")
    private String requestTimeout;
    @Value("${kafka.max.block.ms}")
    private String maxBlockMs;

    private Producer<String, String> producer;


    public KafkaProducerServiceImpl(String serverUrl,String requestTimeout,String maxBlockMs) {
        this.serverUrl = serverUrl;
        this.requestTimeout = requestTimeout;
        this.maxBlockMs = maxBlockMs;
        initConnection();
    }

    @PostConstruct
    public void initConnection() {
        System.out.println("kafka初始化开始......");
        Properties props = new Properties();
        /**
         * Kafka集群连接串，可以由多个host:port组成
         */
        props.put("bootstrap.servers", serverUrl);
        /**
         acks：broker消息确认的模式，有三种：
         0：不进行消息接收确认，即Client端发送完成后不会等待Broker的确认
         1：由Leader确认，Leader接收到消息后会立即返回确认信息
         all：集群完整确认，Leader会等待所有in-sync的follower节点都确认收到消息后，再返回确认信息
         我们可以根据消息的重要程度，设置不同的确认模式。默认为1
         */
        props.put("acks", "all");
        /**
         * retries：发送失败时Producer端的重试次数，默认为0
         */
        props.put("retries", 0);
        /**
         * batch.size：当同时有大量消息要向同一个分区发送时，Producer端会将消息打包后进行批量发送。
         * 如果设置为0，则每条消息都独立发送。默认为16384字节
         */
        props.put("batch.size", 16384);
        /**
         * linger.ms：发送消息前等待的毫秒数，与batch.size配合使用。
         * 在消息负载不高的情况下，配置linger.ms能够让Producer在发送消息前等待一定时间，
         * 以积累更多的消息打包发送，达到节省网络资源的目的。
         * 默认为0
         */
        props.put("linger.ms", 1);
        /**
         * 消息缓冲池大小。尚未被发送的消息会保存在Producer的内存中，
         * 如果消息产生的速度大于消息发送的速度，那么缓冲池满后发送消息的请求会被阻塞。
         * 默认33554432字节（32MB）
         */
        props.put("buffer.memory", 33554432);
        props.put("request.timeout.ms", requestTimeout);// 请求超时时间
        props.put("max.block.ms", maxBlockMs);
        /**
         * 消息key/value的序列器Class，根据key和value的类型决定
         */
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        /**
         * 更多的Producer配置见官网：http://kafka.apache.org/documentation.html#producerconfigs
         */
        try {
            System.out.println(props.toString());
            producer = new KafkaProducer<>(props);
        } catch (Exception e) {
            System.out.println("kafka初始化失败");
            e.printStackTrace();
            throw new RuntimeException("kafka初始化失败",e);
        }
        System.out.println("kafka初始化结束");
    }

    @Override
    @PreDestroy
    public void contextDestroyed() {
        System.out.println("kafka producer关闭中......");
        if (producer != null) {
            producer.close();
        }
        System.out.println("kafka producer关闭成功");
    }

    @Override
    public void sendMessage(String topic, String key, String value) throws RuntimeException{
        try {
            producer.send(new ProducerRecord<>(topic, key, value),
                    //可注册回调函数，也可不添加此参数；producer.send(new ProducerRecord<>(topic, key, value)).get();
                    new Callback() {
                        @Override
                        public void onCompletion(RecordMetadata metadata, Exception exception) {
                            //回调函数可显示消息落在的分区及offset
                            if(metadata!=null) {
                                System.out.println("message send to partition " + metadata.partition() + ", offset: " + metadata.offset());
                            }else{
                                System.out.println("发送失败！");
                            }
                        }
                    }).get();//通过调用get()，来确认是否发送成功，发送失败时调用get()会抛出异常
        } catch (Exception e) {
            throw new RuntimeException("发送异常：",e);
        }
    }
}
