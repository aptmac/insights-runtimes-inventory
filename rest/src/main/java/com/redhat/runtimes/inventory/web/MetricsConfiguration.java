/* Copyright (C) Red Hat 2023 */
package com.redhat.runtimes.inventory.web;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class MetricsConfiguration {

  @ConfigProperty(name = "request.processing.duration.minimumExpectedValue", defaultValue = "1")
  Integer minimumExpectedValue;

  @ConfigProperty(name = "request.processing.duration.maximumExpectedValue", defaultValue = "700")
  Integer maximumExpectedValue;

  @Produces
  @Singleton
  public MeterFilter enableHistogram() {
    return new MeterFilter() {
      @Override
      public DistributionStatisticConfig configure(
          Meter.Id id, DistributionStatisticConfig config) {
        if (id.getName().startsWith("http.server.requests")) {

          return DistributionStatisticConfig.builder()
              .percentilesHistogram(true)
              .minimumExpectedValue(minimumExpectedValue * 1_000_000d)
              .maximumExpectedValue(maximumExpectedValue * 1_000_000d)
              .build()
              .merge(config);
        }
        return config;
      }
    };
  }
}
