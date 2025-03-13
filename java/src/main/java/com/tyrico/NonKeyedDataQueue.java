package com.tyrico;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.DataQueueEntry;

public class NonKeyedDataQueue extends DataQueue{
    com.ibm.as400.access.DataQueue dataQueue;

    private final int INDEFINITE_WAIT = -1;

    @Override
    void initializeDataQueue(AS400 system) {
        String queuePath = "/QSYS.LIB/" + ibMiConfig.getLibraryName() + ".LIB/"
                + ibMiConfig.getInQueueName() + ".DTAQ";
        dataQueue = new com.ibm.as400.access.DataQueue(system, queuePath);
    }
    public String readDataQueue() throws Exception{
            // Read a message from the data queue (wait indefinitely)
            DataQueueEntry entry = dataQueue.read(INDEFINITE_WAIT);
            if (entry != null) {
                String message = entry.getString();
                System.out.println("Received message: " + message);
                return message;
            } else {
                System.out.println("No message received.");
                return "";
            }
    }

    private DataQueueRecord extractValuesFromDataQueueRecord(String record) {
        String key = record.subSequence(0, DataQueueRecord.KEY_LENGTH).toString();
        String data = record.substring(DataQueueRecord.KEY_LENGTH);

        if (key.length() != DataQueueRecord.KEY_LENGTH) {
            throw new RuntimeException("Invalid data queue record. " +
                    "Key is not " + DataQueueRecord.KEY_LENGTH + "characters");
        }

        return new DataQueueRecord(key, data);
    }

    public DataQueueRecord getNextDataQueueRecord() throws Exception {
            String rawValue = readDataQueue();
            DataQueueRecord record = extractValuesFromDataQueueRecord(rawValue);
            return record;
    }
}
