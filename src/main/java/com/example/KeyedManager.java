package com.example;

public class KeyedManager {
    public static void main(String[] args) {
        KafkaConsumerWithKeyedDTAQ consumer = new KafkaConsumerWithKeyedDTAQ();
        KafkaProducerWithDTAQ producer = new KafkaProducerWithDTAQ();

        Thread t1 = new Thread(consumer);
        Thread t2 = new Thread(producer);

        t1.start();
        t2.start();

    }
    
}
