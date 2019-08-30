package no.sysco.middleware.prometheus.kafka.armeria;

import com.typesafe.config.Config;
import java.util.Properties;

public class AppConfig {
  public final Properties properties;

  private AppConfig(Properties properties) {
    this.properties = properties;
  }

  public static AppConfig loadConfig(Config config) {
    // kafka client
    final Properties properties = propsFromConfig(config.getConfig("application.client"));
    final AppConfig appConfig = new AppConfig(properties);
    System.out.println(appConfig);
    return appConfig;
  }

  private static Properties propsFromConfig(Config config) {
    Properties props = new Properties();
    config.entrySet().forEach(entry ->  props.setProperty(entry.getKey(), config.getString(entry.getKey())));
    return props;
  }
}
