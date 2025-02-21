import json
import onnxruntime as ort
import numpy as np
from tyrico import Tyrico

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

tyrico = Tyrico('my_topic','recv_topic', '9.5.12.241', '9092', 'my-consumer-group')

tyrico.set_ask_model(ask_model)
tyrico.run()