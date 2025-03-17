package com.tyrico;

import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.IllegalObjectTypeException;
import com.ibm.as400.access.ObjectDoesNotExistException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class TyricoLib {
    private static final KeyedDataQueue keyedDataQueue = new KeyedDataQueue();
    private static final NonKeyedDataQueue nonKeyedDataQueue = new NonKeyedDataQueue();


    private static final AtomicInteger counter = new AtomicInteger(0);
    private static volatile long lastResetTime = System.currentTimeMillis() / 1000;

    /**
     * Generates a unique key based on timestamp and counter.
     */
    public static String generateKey() {
        long timestampSeconds = System.currentTimeMillis() / 1000;
        if (timestampSeconds - lastResetTime >= 1) {
            counter.set(0);
            lastResetTime = timestampSeconds;
        } else {
            counter.incrementAndGet();
        }

        // Format timestamp as a string
        String timeStr = String.valueOf(timestampSeconds);

        // Format counter as a zero-padded string
        String counterStr = String.format("%07d", counter.get());

        return timeStr + counterStr;
    }

    /**
     * Writes a message to the data queue.
     */
    public static String writeDataQueue(String value) throws AS400SecurityException, IllegalObjectTypeException, ObjectDoesNotExistException, IOException, InterruptedException, ErrorCompletingRequestException {
        String key = generateKey();
        String message = key + value;
        nonKeyedDataQueue.writeDataQueue(message);
        return key;
    }

    /**
     * Reads a message from the data queue.
     */
    public static String readDataQueue(String key) throws AS400SecurityException, IllegalObjectTypeException, ObjectDoesNotExistException, IOException, InterruptedException, ErrorCompletingRequestException {
        return keyedDataQueue.readDataQueue(key);
    }
}

    
    

