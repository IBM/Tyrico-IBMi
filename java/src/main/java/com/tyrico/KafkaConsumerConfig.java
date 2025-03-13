package com.tyrico;

public class KafkaConsumerConfig extends KafkaConfig{

        void setKafkaConfig(){
            String consumerGroupId = envLoader.getEnvironmentVariable("consumerGroupId");

            topic = envLoader.getEnvironmentVariable("receiveTopic");

            properties.put("group.id", consumerGroupId); // Consumer group ID
            properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            properties.put("enable.auto.commit", "true");
            properties.put("auto.offset.reset", "earliest");
        }
}
