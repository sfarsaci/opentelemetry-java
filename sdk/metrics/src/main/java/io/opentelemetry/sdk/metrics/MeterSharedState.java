/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
abstract class MeterSharedState {
  static MeterSharedState create(
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      ImmutableList<MetricsProcessor> metricsProcessors) {
    return new AutoValue_MeterSharedState(
        instrumentationLibraryInfo, new InstrumentRegistry(), metricsProcessors);
  }

  abstract InstrumentationLibraryInfo getInstrumentationLibraryInfo();

  abstract InstrumentRegistry getInstrumentRegistry();

  abstract ImmutableList<MetricsProcessor> getMetricsProcessors();
}
