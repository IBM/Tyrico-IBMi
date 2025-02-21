import time
import logging
from confluent_kafka import Consumer, KafkaException, KafkaError, Producer

# Configure logging
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")

class Kafka:
    """A class to handle consuming and producing Kafka messages"""

    def __init__(self, read_topic, write_topic, server_address, server_port, group_id):
        # Validate required parameters
        for param, name in [(read_topic, "read_topic"), (write_topic, "write_topic"),
                            (server_address, "server_address"), (server_port, "server_port"),
                            (group_id, "group_id")]:
            if param is None:
                raise ValueError(f"{name} must be specified")

        self.read_topic = read_topic
        self.write_topic = write_topic
        self.kafka_config = {
            'bootstrap.servers': f"{server_address}:{server_port}",
            'group.id': group_id,
            'auto.offset.reset': 'earliest',
        }

        # Initialize Kafka Consumer and Producer
        self.consumer = Consumer(self.kafka_config)
        self.producer = Producer(self.kafka_config)

        # Set default implementations
        self.process_message = self._default_process_message
        self.ask_model = self._default_ask_model

        # Subscribe to the topic
        self.consumer.subscribe([read_topic])

    def _default_process_message(self, msg):
        """Default message processing: returns the message unchanged."""
        return msg

    def _default_ask_model(self, _):
        """Placeholder function for ask_model; must be set by the user."""
        raise NotImplementedError("ask_model must be implemented using set_ask_model")

    def set_ask_model(self, func):
        """Allows users to set the model query function dynamically. The model function 
        must take in a 2d array where each element is of the form [key, value]. The function
        must return a 2d array where each element is of the form (key, value). Ex.
        
        def ask_model(batch):
            keys = [x[0] for x in batch]
            values = [json.loads(x[1]) for x in batch]
            outputs = session.run(None, {"float_input": np.array(values).astype(np.float32)})[0]
            ret = []
            for i in range(len(outputs)):
                ret.append((keys[i], outputs[i]))
            return ret
        
        kafka.set_ask_model(ask_model)
        """
        self.ask_model = func

    def set_process_message(self, func):
        """Allows users to set a custom message processing function."""
        self.process_message = func

    def _send_batch(self, batch):
        """Processes and sends a batch of messages."""
        processed_batch = [[key, self.process_message(val)] for key, val in batch]
        return self.ask_model(processed_batch)

    def _send_to_kafka(self, records):
        """Sends processed records to the Kafka write topic."""
        logging.info("Sending batch to Kafka...")
        for key, value in records:
            self.producer.produce(self.write_topic, key=key, value=str(value), callback=self._delivery_report)
        self.producer.poll(0)

    def _delivery_report(self, err, msg):
        """Reports message delivery success/failure."""
        if err:
            logging.error("Message delivery failed: %s", err)
        else:
            logging.info("Message delivered to %s [%s]", msg.topic(), msg.partition())

    def _process_batch(self, batch):
        """Handles batch processing and sending."""
        if batch:
            processed = self._send_batch(batch)
            self._send_to_kafka(processed)
            return []
        return batch

    def run(self):
        """Runs the Kafka consumer and processes messages in batches."""
        try:
            batch = []
            start_time = time.monotonic()

            while True:
                msg = self.consumer.poll(timeout=1.0)  # Adjust timeout as needed

                if msg is None:
                    # If there's data in batch, process it after timeout
                    if batch and (time.monotonic() - start_time > 0.5):
                        batch = self._process_batch(batch)
                        start_time = time.monotonic()
                    continue

                if msg.error():
                    if msg.error().code() == KafkaError._PARTITION_EOF:
                        logging.info("End of partition reached %s, offset %s", msg.partition(), msg.offset())
                    else:
                        raise KafkaException(msg.error())

                # Decode and add message to batch
                decoded_key = msg.key().decode('utf-8')
                decoded_val = msg.value().decode('utf-8')
                logging.info("Adding to batch: %s -> %s", decoded_key, decoded_val)
                batch.append([decoded_key, decoded_val])

                # Process batch if size limit is reached or timeout exceeded
                if len(batch) >= 5 or (time.monotonic() - start_time > 0.5):
                    batch = self._process_batch(batch)
                    start_time = time.monotonic()

        except KeyboardInterrupt:
            logging.info("Consumer interrupted by user.")

        except Exception as e:
            logging.error("Error in Kafka consumer: %s", e)

        finally:
            logging.info("Closing Kafka consumer...")
            self.consumer.close()
