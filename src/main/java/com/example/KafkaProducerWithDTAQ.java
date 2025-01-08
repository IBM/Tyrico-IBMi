package com.example;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.DataQueue;
import com.ibm.as400.access.DataQueueEntry;

import io.github.cdimascio.dotenv.Dotenv;


public class KafkaProducerWithDTAQ implements Runnable {
    DataQueue inDataQueue = null;
    String sendTopic;
    KafkaProducer<String, String> producer;

    public void run() {
        while (true) {
            // This line is blocking so sleep is not needed
            String recordString = readDataQueue();

            // Proper records must have at least 15 chars because the key is 15 chars
            if (recordString.length() > 17) {
                String[] record = extractValuesFromDataQueueRecord(recordString);
                String key = record[0];
                String value = record[1];
                writeKafkaRecord(producer, sendTopic, key, value);
            }
        }
    }

    private String[] extractValuesFromDataQueueRecord(String record) {
        int keyLength = 17;
        String key = record.subSequence(0, keyLength).toString();
        String data = record.substring(keyLength);
        return new String[] { key, data };
    }

    public KafkaProducerWithDTAQ() {
        Dotenv dotenv = Dotenv.load();
        String systemName = dotenv.get("systemName");
        String userName = dotenv.get("userName");
        String password = dotenv.get("password");
        String libraryName = dotenv.get("libraryName");
        String inQueueName = dotenv.get("inQueueName");
        sendTopic = dotenv.get("sendTopic");

        // Initialize system
        AS400 system = new AS400(systemName, userName, password.toCharArray());

        // Initialize input data queue
        String inQueuePath = "/QSYS.LIB/" + libraryName + ".LIB/" + inQueueName + ".DTAQ";
        inDataQueue = new DataQueue(system, inQueuePath);

        // Kafka configuration properties
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092"); // Kafka broker address (local)
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        props.put("acks", "all"); // Ensure all replicas acknowledge the message

        // Create KafkaProducer and KafkaConsumer instance
        producer = new KafkaProducer<>(props);

    }

    void writeKafkaRecord(KafkaProducer<String, String> producer, String topic, String key, String value) {
        System.out.printf("Producing message: Key = %s, Value = %s\n", key, value);
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);

        // Send the message asynchronously
        producer.send(record, (RecordMetadata metadata, Exception exception) -> {
            if (exception != null) {
                exception.printStackTrace(); // Log the error if any
            }
        });
    }

    String readDataQueue() {
        try {
            // Read a message from the data queue (wait indefinitely)
            DataQueueEntry entry = inDataQueue.read(-1); // -1 means wait indefinitely
            if (entry != null) {
                String message = entry.getString();
                System.out.println("Received message: " + message);
                return message;
            } else {
                System.out.println("No message received.");
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
