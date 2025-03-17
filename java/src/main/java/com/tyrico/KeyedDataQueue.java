package com.tyrico;

import com.ibm.as400.access.*;

import java.io.IOException;

public class KeyedDataQueue extends DataQueue{
    com.ibm.as400.access.KeyedDataQueue dataQueue;

    @Override
    void initializeDataQueue(AS400 system) {
        String queuePath = "/QSYS.LIB/" + ibMiConfig.getLibraryName() + ".LIB/"
                + ibMiConfig.getOutQueueNameKeyed() + ".DTAQ";
        dataQueue = new com.ibm.as400.access.KeyedDataQueue(system, queuePath);
        dataQueueName = ibMiConfig.getOutQueueNameKeyed();
    }

    public void writeDataQueue(String key, String data) throws AS400SecurityException, IllegalObjectTypeException, ObjectDoesNotExistException, IOException, InterruptedException, ErrorCompletingRequestException {
            dataQueue.write(key, data);
            if (envLoader.isDebug()){
                System.out.println("Wrote message: " + key + " " + data + "to " + dataQueueName);
            }
    }

    public String readDataQueue(String key) throws AS400SecurityException, IllegalObjectTypeException, ObjectDoesNotExistException, IOException, InterruptedException, ErrorCompletingRequestException {
        KeyedDataQueueEntry keyedDataQueueEntry = dataQueue.read(key);
        if (keyedDataQueueEntry != null) {
            String message = keyedDataQueueEntry.getString();
            if (envLoader.isDebug()){
                System.out.println("Recieved message: " + key + " " + message + " from " + dataQueueName);
            }
            return message;
        } else {
            if (envLoader.isDebug()){
                System.out.println("No message received for key: " + key + " from queue " + dataQueueName);
            }
            return "";
        }
    }
}
