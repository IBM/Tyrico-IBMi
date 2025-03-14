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
your `.env` as `inQueueName` and `outQueueNameKeyed`. 

For more information on how to integrate Tyrico with a c or cpp application check out the [CPP example](./cpp.md)\
For more information on how to integrate Tyrico with an RPG application check out the [RPG example](./rpg.md)

## Setup you custom AI model

1. Download the latest tyrico.py
2. Import the Tyrico class

```python
from tyrico import Tyrico
```

3. Initialize the class

```python
tyrico = Tyrico('<send-topic>','<receive-topic>', '<kafka-server-address>', '<kafka-server-port>', '<kafka-consumer-group>')
```

4. Set you custom model implementation. IMPORTANT: The ask_model function must take in a 2d array where 
each element is of the form [key, value]. The function must return the result of the inferencing as a 2d array
where each element is of the form (key, value). Ex.

```python
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

```python
tyrico.run()
```
