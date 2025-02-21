import json
import onnxruntime as ort
import numpy as np
from tyrico_kafka_consumer import Kafka

modelPath = "./logreg_iris.onnx"
session = ort.InferenceSession(modelPath, providers=ort.get_available_providers())
input_name = session.get_inputs()[0].name

kafka = Kafka('my_topic','recv_topic', '9.5.12.241', '9092', 'my-consumer-group')

def ask_model(batch):
    """Custom ask_model implementation"""
    keys = [x[0] for x in batch]
    values = [json.loads(x[1]) for x in batch]
    outputs = session.run(None, {"float_input": np.array(values).astype(np.float32)})[0]
    ret = []
    for i in range(len(outputs)):
        ret.append((keys[i], outputs[i]))
    return ret

kafka.set_ask_model(ask_model)
kafka.run()