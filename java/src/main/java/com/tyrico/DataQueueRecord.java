package com.tyrico;

public class DataQueueRecord {
    private String key;
    private String value;
    public static final int KEY_LENGTH = 17;

    DataQueueRecord(String key, String value){
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
