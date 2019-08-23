package no.sysco.middleware.prometheus.kafka.producer;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.typesafe.config.ConfigFactory;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import no.sysco.middleware.prometheus.kafka.ClientsJmxCollector;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class Application {
    private static final MediaType CONTENT_TYPE_004 = MediaType.parse(TextFormat.CONTENT_TYPE_004);

    public static void main(String[] args) throws InterruptedException {
        /** load config */
        final AppConfig appConfig = AppConfig.loadConfig(ConfigFactory.load());
        final KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(appConfig.properties);

        /** init health-check, config, metrics */
        final CollectorRegistry collectorRegistry = CollectorRegistry.defaultRegistry;
        new ClientsJmxCollector(kafkaProducer).register(collectorRegistry);

        Server server = new ServerBuilder()
                .http(8080)
                .service("/", (ctx, req) -> HttpResponse.of(kafkaProducer.partitionsFor(appConfig.topicName).toString()))
                .service("/config", (ctx, req) -> HttpResponse.of(appConfig.properties.toString()))
                .service("/metrics", (ctx, req) -> {
                    final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    try (OutputStreamWriter writer = new OutputStreamWriter(stream)) {
                        TextFormat.write004(writer, collectorRegistry.metricFamilySamples());
                    }
                    return HttpResponse.of(HttpStatus.OK, CONTENT_TYPE_004, stream.toByteArray());
                })
                .build();
        CompletableFuture<Void> future = server.start();
        // Wait until the server is ready.
        future.join();

        /** business logic */
        Random random = new Random();
        while (true) {
            final int waitTime = random.nextInt(3_000);
            Thread.sleep(waitTime);
            final String key = String.valueOf(waitTime);
            final String value = waitTime + " milliseconds";
            final ProducerRecord<String, String> record = new ProducerRecord<>(appConfig.topicName, key, value);

            kafkaProducer.send(
                    record,
                    ((metadata, exception) -> {
                        if (exception == null) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("key", key);
                            data.put("value", value);

                            data.put("topic", metadata.topic());
                            data.put("partition", metadata.partition());
                            data.put("offset", metadata.offset());
                            data.put("timestamp", metadata.timestamp());
                            System.out.println(data.toString());
                        } else {
                            exception.printStackTrace();
                        }
                    }));
        }
    }
}
