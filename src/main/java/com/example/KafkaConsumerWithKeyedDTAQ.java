package com.example;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.KeyedDataQueue;

import io.github.cdimascio.dotenv.Dotenv;

public class KafkaConsumerWithKeyedDTAQ implements Runnable {
    KeyedDataQueue outDataQueue = null;
    KafkaConsumer<String, String> consumer;

    public void run() {
        while (true) {
            try {
                // Reading from Kafka is blocking so sleep is not needed here
                ConsumerRecords<String, String> records = readKafkaRecords();
                for (ConsumerRecord<String, String> record : records) {
                    writeDataQueue(record.key(), record.value());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void writeDataQueue(String key, String data) {
        try {
            outDataQueue.write(key, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public KafkaConsumerWithKeyedDTAQ() {
        Dotenv dotenv = Dotenv.load();
        String systemName = dotenv.get("systemName");
        String userName = dotenv.get("userName");
        String password = dotenv.get("password");
        String libraryName = dotenv.get("libraryName");
        String outQueueNameKeyed = dotenv.get("outQueueNameKeyed");
        String receiveTopic = dotenv.get("receiveTopic");

        // Initialize system
        AS400 system = new AS400(systemName, userName, password.toCharArray());

        // Initialize output data queue
        String outQueuePath = "/QSYS.LIB/" + libraryName + ".LIB/" + outQueueNameKeyed + ".DTAQ";
        outDataQueue = new KeyedDataQueue(system, outQueuePath);

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "example-group"); // Consumer group ID
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("enable.auto.commit", "true");
        props.put("auto.offset.reset", "earliest");

        // Create KafkaConsumer instance
        consumer = new KafkaConsumer<>(props);

        // Subscribe to the topic(s)
        consumer.subscribe(Collections.singletonList(receiveTopic));
    }

    ConsumerRecords<String, String> readKafkaRecords() throws Exception {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
        return records;
    }
}
