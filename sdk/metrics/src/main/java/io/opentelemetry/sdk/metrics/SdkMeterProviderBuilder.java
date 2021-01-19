/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.api.metrics.GlobalMetricsProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.SystemClock;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Builder class for the {@link SdkMeterProvider}. Has fully functional default implementations of
 * all three required interfaces.
 */
public final class SdkMeterProviderBuilder {

  private Clock clock = SystemClock.getInstance();
  private Resource resource = Resource.getDefault();
  private final ImmutableList.Builder<MetricsProcessor> metricsProcessorBuilder =
      ImmutableList.builder();

  SdkMeterProviderBuilder() {}

  /**
   * Assign a {@link Clock}.
   *
   * @param clock The clock to use for all temporal needs.
   * @return this
   */
  public SdkMeterProviderBuilder setClock(@Nonnull Clock clock) {
    Objects.requireNonNull(clock, "clock");
    this.clock = clock;
    return this;
  }

  /**
   * Assign a {@link Resource} to be attached to all Spans created by Tracers.
   *
   * @param resource A Resource implementation.
   * @return this
   */
  public SdkMeterProviderBuilder setResource(@Nonnull Resource resource) {
    Objects.requireNonNull(resource, "resource");
    this.resource = resource;
    return this;
  }

  /**
   * Adds a {@link MetricsProcessor}. All the {@link MetricsProcessor} added will be attached to all
   * Metrics created by Meters.
   *
   * @param metricsProcessor A MetricsProcessor implementation.
   * @return this
   */
  public SdkMeterProviderBuilder addMetricsProcessor(@Nonnull MetricsProcessor metricsProcessor) {
    Objects.requireNonNull(metricsProcessor, "metricsProcessor");
    this.metricsProcessorBuilder.add(metricsProcessor);
    return this;
  }

  /**
   * Returns a new {@link SdkMeterProvider} built with the configuration of this {@link
   * SdkMeterProviderBuilder} and registers it as the global {@link
   * io.opentelemetry.api.metrics.MeterProvider}.
   *
   * @see GlobalMetricsProvider
   */
  public SdkMeterProvider buildAndRegisterGlobal() {
    SdkMeterProvider meterProvider = build();
    GlobalMetricsProvider.set(meterProvider);
    return meterProvider;
  }

  /**
   * Returns a new {@link SdkMeterProvider} built with the configuration of this {@link
   * SdkMeterProviderBuilder}. This provider is not registered as the global {@link
   * io.opentelemetry.api.metrics.MeterProvider}. It is recommended that you register one provider
   * using {@link SdkMeterProviderBuilder#buildAndRegisterGlobal()} for use by instrumentation when
   * that requires access to a global instance of {@link
   * io.opentelemetry.api.metrics.MeterProvider}.
   *
   * @see GlobalMetricsProvider
   */
  public SdkMeterProvider build() {
    return new SdkMeterProvider(clock, resource, metricsProcessorBuilder.build());
  }
}
