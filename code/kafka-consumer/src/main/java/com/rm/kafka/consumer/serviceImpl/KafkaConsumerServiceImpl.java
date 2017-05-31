package com.rm.kafka.consumer.serviceImpl;

import com.rm.kafka.consumer.service.KafkaConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 名称：  KafkaConsumerServiceImpl
 * 作者:   rain.wang
 * 日期:   2017/5/29
 * 简介:
 */
@Slf4j
@Service
public class KafkaConsumerServiceImpl implements KafkaConsumerService {

    private Consumer<String, String> consumer;
    Properties props = null;

    @Value("${kafka.consumer.pollTimeout}")
    private long pollTimeout = 100;
    @Value("${kafka.bootstrap.servers}")
    private String serverUrl;
    @Value("${kafka.consumer.group.id}")
    private String groupId;
    @Value("${kafka.consumer.topics}")
    private String topics;

    public KafkaConsumerServiceImpl(String serverUrl,String groupId,String topics) {
        this.serverUrl = serverUrl;
        this.groupId = groupId;
        this.topics = topics;
        initConnection();
    }

    @Override
    public void setTopics(String topics) {
        this.topics = topics;
    }
    @Override
    public String getTopics(){
        return this.topics;
    }

    @Override
    @PostConstruct
    public void initConnection() {
        System.out.println("kafka消费者初始化开始......");
        Properties props = new Properties();
        /**
         * Kafka集群连接串，可以由多个host:port组成
         */
        props.put("bootstrap.servers", serverUrl);
        /**
         * ：Consumer的group id，同一个group下的多个Consumer不会拉取到重复的消息，
         * 不同group下的Consumer则会保证拉取到每一条消息。
         * 注意，同一个group下的consumer数量不能超过分区数。
         */
        props.put("group.id", groupId);
        /**
         * fetch.min.bytes：每次最小拉取的消息大小（byte）。
         * Consumer会等待消息积累到一定尺寸后进行批量拉取。
         * 默认为1，代表有一条就拉一条
         */
        props.put("fetch.min.bytes", "1");
        /**
         * max.partition.fetch.bytes：
         * 每次从单个分区中拉取的消息最大尺寸（byte），默认为1M
         */
        props.put("max.partition.fetch.bytes", "1048576");
        /**
         * 是否自动提交已拉取消息的offset。
         * 提交offset即视为该消息已经成功被消费，
         * 该组下的Consumer无法再拉取到该消息（除非手动修改offset）。
         * 默认为true
         */
        props.put("enable.auto.commit", "true");//标记为false后，需要主动调用commitSync标记处理成功
        /**
         * 自动提交offset的间隔毫秒数，默认5000。
         */
        props.put("auto.commit.interval.ms", "5000");
        /**
         *   全部的Consumer配置见官方文档：http://kafka.apache.org/documentation.html#newconsumerconfigs
         */
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        try {
            if(consumer != null){
                consumer.close();
                consumer = null;
            }
            consumer = new KafkaConsumer<String,String>(props);
            consumer.subscribe(Arrays.asList(topics.split(",")));
            System.out.println("kafka消费者初始化结束");
        } catch (Exception e) {
            System.out.println("kafka初始化失败");
            e.printStackTrace();
            throw new RuntimeException("kafka初始化失败",e);
        }
    }

    /**
     * 返回所有订阅的topics列表，以及其分区信息PartitionInfo
     * @return
     */
    @Override
    public Map<String, List<PartitionInfo>> listTopics(){
        return consumer.listTopics();
    }

    @Override
    public ConsumerRecords<String, String> poll(){
        return consumer.poll(pollTimeout);
    }

    @Override
    public void close(){
        consumer.close();
    }

    @Override
    public void commitSync() {
        consumer.commitSync();
    }

}
