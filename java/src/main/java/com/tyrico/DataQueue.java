package com.tyrico;

import com.ibm.as400.access.AS400;


public abstract class DataQueue {
    IBMiConfig ibMiConfig;

    DataQueue(){
        ibMiConfig = new IBMiConfig();
        AS400 system = new AS400(
                ibMiConfig.getSystemName(),
                ibMiConfig.getUsername(),
                ibMiConfig.getPassword().toCharArray());

        initializeDataQueue(system);
    }

    abstract void initializeDataQueue(AS400 system);
}
