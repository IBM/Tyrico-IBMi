from confluent_kafka import Consumer, KafkaException, KafkaError, Producer
import time
import onnxruntime as ort
import numpy as np
import json

modelPath = "./logreg_iris.onnx"
session = ort.InferenceSession(modelPath, providers=ort.get_available_providers())
input_name = session.get_inputs()[0].name

def delivery_report(err, msg):
    """ Called once for each message produced to indicate delivery result.
        Triggered by poll() or flush(). """
    if err is not None:
        print('Message delivery failed: {}'.format(err))
    else:
        print('Message delivered to {} [{}]'.format(msg.topic(), msg.partition()))
        
        
def ask_model(batch):
    # return batch
    keys = [x[0] for x in batch]
    values = [json.loads(x[1]) for x in batch]
    outputs = session.run(None, {"float_input": np.array(values).astype(np.float32)})[0]
    
    ret = []
    for i in range(len(outputs)):
        ret.append(((keys[i], outputs[i])))
    return ret
        
def process_message(msg):
    return msg

def sendBatch(batch):
    processed_batch = []
    for msg in batch:
        key, val = msg
        encoded_val = process_message(val)
        processed_batch.append([key, encoded_val])
    
    ret = ask_model(processed_batch)
    return ret

def sendToKafka(records):
    print("Sending to Kafka")
    for record in records:
        producer.produce(topic_2, key=record[0], value=str(record[1]), callback=delivery_report)
    producer.poll(0)
    

# Kafka Consumer configuration
conf = {
    'bootstrap.servers': '9.5.12.241:9092',  # Replace with your Kafka server address
    'group.id': 'my-consumer-group',  # Consumer group ID
    'auto.offset.reset': 'earliest',  # Start reading from the earliest message
}

# Create Consumer instance
consumer = Consumer(conf)
producer = Producer(conf)

# Kafka topic to consume messages from
topic = 'my_topic'  # Replace with your topic name
topic_2 = 'recv_topic'

# Subscribe to the topic
consumer.subscribe([topic])

# Start consuming messages
try:
    start = time.time()
    batch = []
    while True:
        # Poll for a message (this will block until a message is received or timeout occurs)
        msg = consumer.poll(timeout=1.0)  # Adjust timeout as necessary

        if msg is None:
            # Check if there is a batch to be sent
            if time.time() - start > 5 and len(batch) > 0:
                res = sendBatch(batch)
                sendToKafka(res)
                batch = []
                start = time.time()
                
            continue
        if msg.error():
            # Handle errors (if any)
            if msg.error().code() == KafkaError._PARTITION_EOF:
                print(f"End of partition reached {msg.partition}, offset {msg.offset()}")
            else:
                raise KafkaException(msg.error())
        else:
            # Process the message
            decoded_val = msg.value().decode('utf-8')
            decoded_key = msg.key().decode('utf-8')
            print("Adding to batch", decoded_key, decoded_val)
            batch.append([decoded_key, decoded_val])

            if(len(batch) >= 5 or time.time() - start > 5):
                res = sendBatch(batch)
                sendToKafka(res)
                batch = []
                start = time.time()

except KeyboardInterrupt:
    print("Consumer interrupted")

finally:
    # Close the consumer when done
    consumer.close()
