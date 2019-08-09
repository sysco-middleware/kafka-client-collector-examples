package no.sysco.middleware.prometheus.kafka;

import io.prometheus.client.exporter.HTTPServer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

public class StreamExample {

    public static void main(String[] args) throws IOException {
        final String topic1 = "topic-1";
        final String topic2 = "topic-2";
        final String id = UUID.randomUUID().toString();
        final KafkaStreams kafkaStreams = new KafkaStreams(buildTopology(topic1, topic2), getStreamProps(id));

        kafkaStreams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));

        // http server
        HTTPServer server = new HTTPServer(8083);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        KafkaClientsJmxExports.initialize(kafkaStreams);
    }

    static Topology buildTopology(String in, String out) {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        streamsBuilder.stream(in)
                .mapValues(v -> {
                    System.out.printf("Value [%s]\n", v);
                    return v + " new value";
                })
                .to(out);
        return streamsBuilder.build();
    }

    static Properties getStreamProps(String id) {
        final Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "stream-metrics");
        properties.put(StreamsConfig.CLIENT_ID_CONFIG, id);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        return properties;
    }
}
