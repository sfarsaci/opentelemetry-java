/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.Instrument;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

public class SynchronousInstrumentAccumulatorTest {
  private static final InstrumentDescriptor DESCRIPTOR =
      InstrumentDescriptor.create(
          "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.DOUBLE);
  private final TestClock testClock = TestClock.create();
  private final Aggregator<Long> aggregator =
      AggregatorFactory.lastValue()
          .create(
              Resource.getEmpty(), InstrumentationLibraryInfo.create("test", "1.0"), DESCRIPTOR);

  @Test
  void sameAggregator_ForSameLabelSet() {
    SynchronousInstrumentAccumulator<?> accumulator =
        new SynchronousInstrumentAccumulator<>(
            aggregator, new InstrumentProcessor<>(aggregator, testClock.now()), ImmutableList.of());
    AggregatorHandle<?> aggregatorHandle =
        accumulator.bind(Labels.of("K", "V"), mock(Instrument.class));
    AggregatorHandle<?> duplicateAggregatorHandle =
        accumulator.bind(Labels.of("K", "V"), mock(Instrument.class));
    try {
      assertThat(duplicateAggregatorHandle).isSameAs(aggregatorHandle);
      accumulator.collectAll(testClock.now());
      AggregatorHandle<?> anotherDuplicateAggregatorHandle =
          accumulator.bind(Labels.of("K", "V"), mock(Instrument.class));
      try {
        assertThat(anotherDuplicateAggregatorHandle).isSameAs(aggregatorHandle);
      } finally {
        anotherDuplicateAggregatorHandle.release();
      }
    } finally {
      duplicateAggregatorHandle.release();
      aggregatorHandle.release();
    }

    // At this point we should be able to unmap because all references are gone. Because this is an
    // internal detail we cannot call collectAll after this anymore.
    assertThat(aggregatorHandle.tryUnmap()).isTrue();
  }

  @Test
  public void metricsProcessorsAreInvokedInChain() {
    final Labels labels = Labels.of("K", "V");

    final Instrument instrument = mock(Instrument.class);
    final MetricsProcessor mock1 = mock(MetricsProcessor.class);
    final MetricsProcessor mock2 = mock(MetricsProcessor.class);
    final Labels updatedLabels = Labels.of("K2", "V2");

    // first invokation
    when(mock1.onLabelsBound(Context.current(), instrument, labels))
        .thenReturn(Labels.of("K1", "V1"));
    when(mock2.onLabelsBound(Context.current(), instrument, Labels.of("K1", "V1")))
        .thenReturn(updatedLabels);

    // second invokation
    when(mock1.onLabelsBound(Context.current(), instrument, updatedLabels))
        .thenReturn(updatedLabels);
    when(mock2.onLabelsBound(Context.current(), instrument, updatedLabels))
        .thenReturn(updatedLabels);

    SynchronousInstrumentAccumulator<?> accumulator =
        new SynchronousInstrumentAccumulator<>(
            aggregator,
            new InstrumentProcessor<>(aggregator, testClock.now()),
            ImmutableList.of(mock1, mock2));

    AggregatorHandle<?> aggregatorHandle = accumulator.bind(labels, instrument);
    AggregatorHandle<?> aggregatorHandle2 = accumulator.bind(updatedLabels, instrument);
    assertThat(aggregatorHandle2).isSameAs(aggregatorHandle);
  }
}
