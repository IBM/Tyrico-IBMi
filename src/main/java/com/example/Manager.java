package com.example;

public class Manager {
    public static void main(String[] args) {
        KafkaConsumerWithDTAQ consumer = new KafkaConsumerWithDTAQ();
        KafkaProducerWithDTAQ producer = new KafkaProducerWithDTAQ();

        Thread t1 = new Thread(consumer);
        Thread t2 = new Thread(producer);

        t1.start();
        t2.start();

    }
    
}
