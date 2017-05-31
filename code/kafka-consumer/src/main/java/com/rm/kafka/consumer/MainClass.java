package com.rm.kafka.consumer;

import com.rm.kafka.consumer.service.KafkaConsumerService;
import com.rm.kafka.consumer.serviceImpl.KafkaConsumerServiceImpl;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * 名称：  MainClass
 * 作者:   rain.wang
 * 日期:   2017/5/29
 * 简介:
 */
public class MainClass {


    public static void main(String[] args) {

        KafkaConsumerService kafkaProducerService;
        //初始化KafkaProducerService
        InputStream in = ClassLoader.getSystemResourceAsStream("kafka.properties");
        try {
            Properties props = new Properties();
            props.load(new BufferedReader(new InputStreamReader(in,"utf-8")));
            String serverUrl = (String) props.get("kafka.bootstrap.servers");
            String groupId = (String) props.get("kafka.consumer.group.id");
            String topics = (String) props.get("kafka.consumer.topics");
            kafkaProducerService = new KafkaConsumerServiceImpl(serverUrl,groupId,topics);
        } catch (IOException e1) {
            System.out.println("获取配置文件异常");
            e1.printStackTrace();
            return;
        }catch (RuntimeException e1) {
            e1.printStackTrace();
            return;
        }

        String topics;
        while (true){
            System.out.println("请输入topics，以逗号分隔(输入quit退出，默认为test)：");
            topics = System.console() == null?null:System.console().readLine();
            if("quit".equals(topics)){
                kafkaProducerService.close();
                break;
            }else{
                if(topics != null && !topics.isEmpty() && !topics.equals(kafkaProducerService.getTopics())){
                    //非默认topic
                    kafkaProducerService.setTopics(topics);
                    kafkaProducerService.initConnection();
                }
                System.out.println("开始获取消息：");
                try {
                    while (true) {
                        ConsumerRecords<String, String> consumerRecords = kafkaProducerService.poll();
                        for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                            insertIntoDb(consumerRecord);
                        }
                    }
                }finally {
                    kafkaProducerService.close();
                }
            }
        }
    }

    private static void insertIntoDb(ConsumerRecord<String, String> consumerRecord) {
        System.out.println("收到新消息：" +
                "partition("+consumerRecord.partition() + ")," +
                "offset("+consumerRecord.offset() + ")," +
                "key(" + consumerRecord.key() + ")," +
                "value(" + consumerRecord.value()+")");
        System.out.println("处理成功！");
        System.exit(0);
    }
}
