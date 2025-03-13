package com.tyrico;

import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaProducerConfig extends KafkaConfig {

        void setKafkaConfig(){
            topic = envLoader.getEnvironmentVariable("sendTopic");

            properties.put("key.serializer", StringSerializer.class.getName());
            properties.put("value.serializer", StringSerializer.class.getName());
            properties.put("acks", "all"); // Ensure all replicas acknowledge the message
        }
}
