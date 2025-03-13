package com.tyrico;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;

public class KafkaConsumerWithKeyedDTAQ implements Runnable {
    KeyedDataQueue outputKeyedDataQueue;
    KafkaConsumer<String, String> consumer;

    public void run() {
        while (true) {
            try {
                // Reading from Kafka is blocking so sleep is not needed here
                ConsumerRecords<String, String> records = readKafkaRecords();
                for (ConsumerRecord<String, String> record : records) {
                    outputKeyedDataQueue.writeDataQueue(record.key(), record.value());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public KafkaConsumerWithKeyedDTAQ() {
        outputKeyedDataQueue = new KeyedDataQueue();
        KafkaConsumerConfig kafkaConsumerConfig = new KafkaConsumerConfig();

        consumer = new KafkaConsumer<>(kafkaConsumerConfig.getProperties());
        
        // Subscribe to the topic(s)
        consumer.subscribe(Collections.singletonList(kafkaConsumerConfig.getTopic()));
    }

    ConsumerRecords<String, String> readKafkaRecords() throws Exception {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
        return records;
    }
}
