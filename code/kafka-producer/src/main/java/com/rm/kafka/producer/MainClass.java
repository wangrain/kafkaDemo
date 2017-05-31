package com.rm.kafka.producer;

import com.rm.kafka.producer.service.KafkaProducerService;
import com.rm.kafka.producer.serviceImpl.KafkaProducerServiceImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

/**
 * 名称：  MainClass
 * 作者:   rain.wang
 * 日期:   2017/5/29
 * 简介:
 */
public class MainClass {


    public static void main(String[] args) {
        KafkaProducerService kafkaProducerService;
        //初始化KafkaProducerService
        InputStream in = ClassLoader.getSystemResourceAsStream("kafka.properties");
        try {
            Properties props = new Properties();
            props.load(new BufferedReader(new InputStreamReader(in,"utf-8")));
            String serverUrl = (String) props.get("kafka.bootstrap.servers");
            String requestTimeout = (String) props.get("kafka.request.timeout.ms");
            String maxBlockMs = (String) props.get("kafka.max.block.ms");

            System.out.println("请输入serverUrl，以逗号分隔(默认为"+serverUrl+")：");
            String inStr = System.console().readLine();
            if(inStr != null && !inStr.isEmpty()){
                serverUrl = inStr;
            }

            kafkaProducerService = new KafkaProducerServiceImpl(serverUrl,requestTimeout,maxBlockMs);
        } catch (IOException e1) {
            System.out.println("获取配置文件异常");
            e1.printStackTrace();
            return;
        }catch (RuntimeException e1) {
            e1.printStackTrace();
            return;
        }

        String topic;
        while (true){
            System.out.println("请输入topic(输入quit退出)：");
            topic = System.console().readLine();
            if(topic != null && !topic.isEmpty()){
                if("quit".equals(topic)){
                    kafkaProducerService.contextDestroyed();
                    break;
                }else{
                    while(true){
                        System.out.println("请输入key和message，以冒号分隔(输入quit退出,输入auto自动发送)：");
                        String kv = System.console().readLine();
                        if ("quit".equals(kv)) break;
                        if ("auto".equals(kv)) MainClass.auto(topic,kafkaProducerService);
                        if (kv==null||kv.isEmpty()||"auto".equals(kv)) continue;

                        String key ;
                        String message ;
                        if (kv.split(":").length==2) {
                            key = kv.split(":")[0];
                            message = kv.substring(kv.indexOf(":") +1);
                        }else{
                            key = new Date().getTime() + "" + new Random().nextLong();
                            message = kv;
                        }
                        try {
                            kafkaProducerService.sendMessage(topic,key,message);
                        } catch (RuntimeException e) {
                            System.out.println("发送失败：");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * 自动发送
     * @param kafkaProducerService
     */
    public static void auto(String topic,KafkaProducerService kafkaProducerService){

        String key ;
        String message ;
        long millis;
        long num = 1;
        System.out.println("请输入发送间隔（毫秒），默认为1秒：");
        try {
            String millisStr = System.console().readLine();
            millis = Long.valueOf(millisStr);
        }catch (NumberFormatException e){
            System.out.println("格式不正确，默认1秒。");
            millis = 1000L;
        }

        while(true){
            key = new Date().getTime() + "" + new Random().nextLong();
            message = "auto-message-" + num;
            try {
                kafkaProducerService.sendMessage(topic,key,message);
                num ++;
                Thread.sleep(millis);
            } catch (RuntimeException e) {
                System.out.println("发送失败：");
                e.printStackTrace();
                break;
            } catch (InterruptedException e) {
                System.out.println("系统异常：");
                e.printStackTrace();
                break;
            }
        }

    }
}
