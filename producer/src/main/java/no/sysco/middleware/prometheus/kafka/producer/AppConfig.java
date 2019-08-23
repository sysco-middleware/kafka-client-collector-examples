package no.sysco.middleware.prometheus.kafka.producer;

import com.typesafe.config.Config;

import java.util.Properties;

public class AppConfig {
  public final Properties properties;
  public final String topicName;
  private AppConfig(final Properties properties, final String topicName) {
    this.properties = properties;
    this.topicName = topicName;
  }

  public static AppConfig loadConfig(Config config) {
    // kafka client
    final Properties properties = propsFromConfig(config.getConfig("application.client"));
    final String topicName = config.getString("application.topic");
    final AppConfig appConfig = new AppConfig(properties, topicName);
    System.out.println(appConfig);
    return appConfig;
  }

  private static Properties propsFromConfig(Config config) {
    Properties props = new Properties();
    config.entrySet().forEach(entry ->  props.setProperty(entry.getKey(), config.getString(entry.getKey())));
    return props;
  }

  @Override
  public String toString() {
    return "AppConfig{" +
            "properties=" + properties +
            ", topicName='" + topicName + '\'' +
            '}';
  }
}
