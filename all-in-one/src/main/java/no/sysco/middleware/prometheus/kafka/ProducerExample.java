package no.sysco.middleware.prometheus.kafka;

import io.prometheus.client.exporter.HTTPServer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Instant;
import java.util.Properties;
import java.util.UUID;

public class ProducerExample {
    public static void main(String[] args) throws Exception {

        final String id1 = UUID.randomUUID().toString();
        final String id2 = UUID.randomUUID().toString();
        final String topic1 = "topic-1";
        final String topic2 = "topic-2";
        final KafkaProducer<String, String> kafkaProducer1 = new KafkaProducer<>(getProducerProps(id1));
        final KafkaProducer<String, String> kafkaProducer2 = new KafkaProducer<>(getProducerProps(id2));

        final HTTPServer server = new HTTPServer(8081);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        KafkaClientsJmxExports.initialize(kafkaProducer1, kafkaProducer2);

        while (true) {
            Thread.sleep(5_000);
            long now = Instant.now().toEpochMilli();

            kafkaProducer1.send(
                    new ProducerRecord<>(topic1, now + " p1", now + " milliseconds"),
                    (metadata, exception) -> {
                        if (exception == null) {
                            System.out.println("successfully sent");
                        } else {
                            System.out.println("fail sent");
                        }
                    }
            );
            kafkaProducer1.send(
                    new ProducerRecord<>(topic2, now + " p1", now + " milliseconds"),
                    (metadata, exception) -> {
                        if (exception == null) {
                            System.out.println("successfully sent");
                        } else {
                            System.out.println("fail sent");
                        }
                    }
            );

            Thread.sleep(5_000);
            kafkaProducer2.send(
                    new ProducerRecord<>(topic1, now + " p2", now + " milliseconds"),
                    (metadata, exception) -> {
                        if (exception == null) {
                            System.out.println("successfully sent");
                        } else {
                            System.out.println("fail sent");
                        }
                    }
            );
        }
    }

    static Properties getProducerProps(String id) {
        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, id);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return properties;
    }
}
