package com.tyrico;
import com.tyrico.TyricoLib;
public class SampleCustomerProgram {

    private static final String[] FLOWERS = {
            "[7.47,2.47,5.34,2.34]", "[5.12,2.28,6.81,0.39]", "[7.16,3.70,4.58,1.24]",
            "[4.99,2.92,4.22,2.23]", "[6.09,4.31,2.46,1.96]", "[6.22,4.29,6.53,1.46]",
            "[5.54,2.13,2.74,0.43]", "[7.46,3.85,4.70,1.51]", "[7.76,3.43,6.54,0.64]",
            "[7.68,3.65,6.63,2.35]", "[7.03,2.31,6.68,0.78]", "[6.44,2.68,5.40,2.13]",
            "[5.97,2.56,6.14,0.56]", "[4.56,2.60,3.33,0.79]", "[4.94,2.94,6.77,0.68]",
            "[7.85,2.82,4.00,1.28]", "[4.66,4.10,2.66,2.31]", "[7.43,2.85,3.06,0.56]",
            "[5.29,2.62,2.28,0.32]", "[4.70,2.54,1.29,2.42]", "[5.92,4.22,3.32,0.81]",
            "[6.64,3.18,2.29,1.13]", "[5.25,2.10,3.07,0.47]", "[6.00,2.44,1.75,2.43]",
            "[6.10,2.44,5.30,0.19]", "[7.13,4.25,5.67,0.74]", "[6.80,2.37,5.09,0.37]",
            "[7.76,2.48,2.66,0.63]"
    };

    private static final int DATA_LENGTH = FLOWERS.length;
    private static final int NUM_MESSAGES = 100000;

    public static void main(String[] args) {
        String[] keys = new String[NUM_MESSAGES];

        // Writing messages to data queue
        for (int i = 0; i < NUM_MESSAGES; i++) {
            String message = FLOWERS[i % DATA_LENGTH];
            System.out.println("Sending message " + i + ": " + message);

            try {
                String key = TyricoLib.writeDataQueue( message);
                keys[i] = key;
            } catch (Exception e) {
                keys[i] = "";
                e.printStackTrace();
            }
        }

        // Reading messages from data queue
        for (int i = 0; i < NUM_MESSAGES; i++) {
            String returnedKey = keys[i];
            if (!returnedKey.isEmpty()) {
                try {
                    String output = TyricoLib.readDataQueue(returnedKey);

                    if (!output.equals("")){
                        System.out.println("Received message key " + returnedKey + ", value " + output);
                    } else {
                        System.out.println("Could not find key " + returnedKey + " in dataqueue");
                    }

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}

