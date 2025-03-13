package com.tyrico;

import java.util.Properties;

public abstract class KafkaConfig {
    Properties properties = new Properties();
    String topic;
    EnvLoader envLoader;

    KafkaConfig(){
        envLoader = new EnvLoader();
        String kafkaHostname = envLoader.getEnvironmentVariable("kafkaHostname");
        String kafkaPort = envLoader.getEnvironmentVariable("kafkaPort");
        properties.put("bootstrap.servers", kafkaHostname + ":" + kafkaPort);

        setKafkaConfig();
    }

    abstract void setKafkaConfig();

    public Properties getProperties() {
        return properties;
    }

    public String getTopic(){
        return topic;
    }
}
