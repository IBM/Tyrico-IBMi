package com.tyrico;

import com.ibm.as400.access.AS400;

public class KeyedDataQueue extends DataQueue{
    com.ibm.as400.access.KeyedDataQueue dataQueue;

    @Override
    void initializeDataQueue(AS400 system) {
        String queuePath = "/QSYS.LIB/" + ibMiConfig.getLibraryName() + ".LIB/"
                + ibMiConfig.getOutQueueNameKeyed() + ".DTAQ";
        dataQueue = new com.ibm.as400.access.KeyedDataQueue(system, queuePath);
    }

    public void writeDataQueue(String key, String data) {
        try {
            dataQueue.write(key, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
