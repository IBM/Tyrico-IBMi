package com.tyrico;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public class KafkaProducerExample {

    public static void main(String[] args) throws CsvValidationException, NumberFormatException {

        // Kafka configuration properties
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");  // Kafka broker address (local)
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        props.put("acks", "all");  // Ensure all replicas acknowledge the message

        Properties consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", "localhost:9092"); 
        consumerProps.put("group.id", "example-group");          // Consumer group ID
        consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put("enable.auto.commit", "true");        
        consumerProps.put("auto.offset.reset", "earliest");      

        // Create KafkaProducer and KafkaConsumer instance
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);

        // Subscribe to the topic(s)
        consumer.subscribe(Collections.singletonList("recv_topic")); 

        // Kafka topic to send messages to
        String topic = "my_topic"; 

        // Load the test data
        List<double[]> irisData = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader("./iris_training_data.csv"))) {
            String[] line;
            // Skip the header row
            reader.readNext();

            // Read each line and parse it into a double array
            while ((line = reader.readNext()) != null) {
                double[] row = new double[line.length];
                for (int i = 0; i < line.length; i++) {
                    row[i] = Double.parseDouble(line[i]);
                }
                irisData.add(row);
            }

        } catch (IOException e) {
            System.err.println("Error reading the CSV file: " + e.getMessage());
        }

        // Convert the List to a 2D array
        double[][] irisArray = irisData.toArray(new double[0][]);

        // Send all data to Kafka topic. Start at 1 because first row is column headers.
        int counter = 0;
        long lastResetTime = System.currentTimeMillis() / 1000;
        for (int i = 1; i < irisArray.length; i++) {
            long timestampSeconds = System.currentTimeMillis() / 1000;
            if (timestampSeconds  - lastResetTime >= 1) {
                counter = 0;
                lastResetTime = timestampSeconds;
            } else {
                counter = counter + i;
            }
            String key = String.valueOf(timestampSeconds) + String.valueOf(counter);
            double data[] = irisArray[i];
            String dataString = Arrays.toString(data);
            System.out.printf("Producing message: Key = %s, Value = %s\n", key, dataString);
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, dataString);

            // Send the message asynchronously
            producer.send(record, (RecordMetadata metadata, Exception exception) -> {
                if (exception != null) {
                    exception.printStackTrace();  // Log the error if any
                }
            });
        }
        producer.close();


        try {
            int count = 0;
            while (true && count < 100000) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    count++;
                    System.out.printf("Consumed message: Key = %s, Value = %s, Partition = %d, Offset = %d%n",
                            record.key(), record.value(), record.partition(), record.offset());
                    System.out.printf("Count: %d ", count);
                }
            }
        } finally {
            // Step 5: Close the consumer gracefully
            consumer.close();
        }
    }
}
