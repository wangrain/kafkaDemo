package com.rm.kafka.producer.service;


public interface KafkaProducerService {
    void contextDestroyed();

    void sendMessage(final String topic, final String key, final String value) throws  RuntimeException;
}
