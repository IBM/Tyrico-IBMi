# tyricov2


1. Deploy kafka on IBM i: https://ibmi-oss-docs.readthedocs.io/en/latest/kafka/README.html#deploying-kafka-on-ibm-i

2. 
Run python3.9 -m venv venv

source venv/bin/activate     

pip install -r requirements.txt

Run which python, and then use that path as the vscode python interpreter. Run vscode command
`python: select interpreter`, and  paste in the path.
(venv) ➜  tyricov2 git:(main) ✗ which python                                                                  
/Users/zakjonat/tyricov2/venv/bin/python

Make tyrico dir on ibm i
```mkdir tyrico```


Send java files to IBM i
```scp -r iris_training_data.csv pom.xml src jonathan@p8adt05.rch.stglabs.ibm.com:~/tyrico```

Compile
```mvn package```

Start the consumer on mac:
```python kafka_consumer.py```

Start the producer on ibm i:



