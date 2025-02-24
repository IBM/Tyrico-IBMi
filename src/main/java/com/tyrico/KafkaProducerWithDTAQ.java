package com.tyrico;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class KafkaProducerWithDTAQ implements Runnable {
    NonKeyedDataQueue inputDataQueue;
    KafkaProducer<String, String> producer;
    KafkaConfig kafkaProducerConfig;

    public void run() {
        while (true) {
            try {
                // This method call is blocking so sleep is not needed
                DataQueueRecord dataQueueRecord = inputDataQueue.getNextDataQueueRecord();
                writeKafkaRecord(dataQueueRecord);
            } catch (Exception e){
                System.out.println("Unable to get next data queue record");
                e.printStackTrace();
            }
        }
    }

    public KafkaProducerWithDTAQ() {
        inputDataQueue = new NonKeyedDataQueue();
        kafkaProducerConfig = new KafkaProducerConfig();

        // Create KafkaProducer
        producer = new KafkaProducer<>(kafkaProducerConfig.getProperties());
    }

    void writeKafkaRecord(DataQueueRecord dataQueueRecord) {
        String key = dataQueueRecord.getKey();
        String value = dataQueueRecord.getValue();
        System.out.printf("Producing message: Key = %s, Value = %s\n", key, value);
        ProducerRecord<String, String> record = new ProducerRecord<>(kafkaProducerConfig.getTopic(), key, value);

        // Send the message asynchronously
        producer.send(record, (RecordMetadata metadata, Exception exception) -> {
            if (exception != null) {
                exception.printStackTrace(); // Log the error if any
            }
        });
    }
}
