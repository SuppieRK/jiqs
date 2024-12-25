package io.github.suppierk.jiqs.configuration;

import io.github.suppierk.inject.Provides;
import jakarta.inject.Singleton;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ClassPathConfigSourceBuilder;

@Singleton
public class GestaltProvider {
  @Provides
  @Singleton
  public Gestalt gestalt() throws GestaltException {
    final var config =
        new GestaltBuilder()
            .addSource(
                ClassPathConfigSourceBuilder.builder().setResource("application.yml").build())
            .build();

    config.loadConfigs();

    return config;
  }
}
