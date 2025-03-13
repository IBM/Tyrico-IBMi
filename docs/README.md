# Tyrico: How to run 


## Setup kafka on IBM i
1. Deploy kafka on IBM i: https://ibmi-oss-docs.readthedocs.io/en/latest/kafka/README.html#deploying-kafka-on-ibm-i

## Tyrico IBM i setup

### Running the Tyrico jar
1. Download the latest `tyrico-1.0-SNAPSHOT.jar` and `env.sample`
2. In the directory where you will run Tyrico from, copy the `env.sample` file to `.env` and fill out the required values
3. Run the jar file using the command
```java -jar tyrico-1.0-SNAPSHOT.jar```

Now Tyrico will read messages from the input data queue, and send them to the kafka `sendTopic` specified. The data will then be read off of kafka by `tyrico.py`, processed by your custom model, written back to the kafka `receiveTopic`, and finally written to your keyed output data queue.

### Integrating with your IBM i application
In order to integrate Tyrico with your IBM i application, you need to write the data that you want inferenced to
your write data queue, and you will read the results from your keyed read data queue. You must use the same names as specified in
your `.env` as `inQueueName` and `outQueueNameKeyed`. At the moment we only support cpp applications, but we will be supporting 
other application types very soon including RPGLE. 

1. Download the `tyrico.cpp`, and `tyrico.h` from the latest release.
2. Include tyrico in your cpp application.
```
#include "tyrico.h"
```
3. Write the data that you want to be processed to the `inQueueName` data queue. Note the unique key for this entry
is returned.
```
string key = writeDataQueue(DTAQ_IN, DTAQ_LIB, message);
```
4. Retrieve the result of the inferencing by reading from the `outQueueNameKeyed` data queue.
```
string res = readDataQueue(DTAQ_OUT, DTAQ_LIB, key);
```

## Setup you custom AI model

1. Download the latest tyrico.py
2. Import the Tyrico class
```
from tyrico import Tyrico
```
3. Initialize the class
```
tyrico = Tyrico('<send-topic>','<receive-topic>', '<kafka-server-address>', '<kafka-server-port>', '<kafka-consumer-group>')
```
4. Set you custom model implementation. IMPORTANT: The ask_model function must take in a 2d array where 
each element is of the form [key, value]. The function must return the result of the inferencing as a 2d array
where each element is of the form (key, value). Ex.
```
modelPath = "./logreg_iris.onnx"
session = ort.InferenceSession(modelPath, providers=ort.get_available_providers())

def ask_model(batch):
    """Custom ask_model implementation"""
    keys = [x[0] for x in batch]
    values = [json.loads(x[1]) for x in batch]
    outputs = session.run(None, {"float_input": np.array(values).astype(np.float32)})[0]
    ret = []
    for i in range(len(outputs)):
        ret.append((keys[i], outputs[i]))
    return ret

tyrico.set_ask_model(ask_model)
```
5. Run the tyrico_kafka_consumer
```
tyrico.run()
```