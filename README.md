# Tyrico: How to run 

## Setup consumer locally

1. Clone to your local system `https://github.com/jonnyz32/tyricov2.git`
2. Install python 3.9
3. Setup your python virtual environment
```
cd tyricov2
python3.9 -m venv venv
source venv/bin/activate     
pip install -r requirements.txt
```
4. Select python interpreter in vscode. Get python path. 
```
which python                                                                  
/Users/zakjonat/tyricov2/venv/bin/python
```
Then run vscode command
`python: select interpreter`, and  paste in the path.


## Setup kafka on IBM i
1. Deploy kafka on IBM i: https://ibmi-oss-docs.readthedocs.io/en/latest/kafka/README.html#deploying-kafka-on-ibm-i

## Setup producer on IBM i

1. Make tyrico dir on ibm i
```mkdir ~/tyrico```

2. Send java files to IBM i
```scp -r iris_training_data.csv pom.xml src jonathan@p8adt05.rch.stglabs.ibm.com:~/tyrico```

3. Compile
```
cd ~/tyrico
mvn package
```

4. Start the producer on ibm i:
```
java -jar target/kafka-producer-1.0-SNAPSHOT.jar
```

## Start the consumer locally
1. `python kafka_consumer.py`

## Troubleshooting:
If the kafka doesn't automatically create the topics `my_topic` and `recv_topic`, you may need to create it manually.

Ex: `kafka-topics.sh --create --topic my_topic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 2`



