package com.rm.kafka.consumer.service;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Map;

/**
 * 名称：  KafkaConsumerService
 * 作者:   rain.wang
 * 日期:   2017/5/29
 * 简介:
 */
public interface KafkaConsumerService {
    void setTopics(String topics);

    String getTopics();

    void initConnection();

    Map<String, List<PartitionInfo>> listTopics();

    ConsumerRecords<String, String> poll();

    void close();

    void commitSync();
}
