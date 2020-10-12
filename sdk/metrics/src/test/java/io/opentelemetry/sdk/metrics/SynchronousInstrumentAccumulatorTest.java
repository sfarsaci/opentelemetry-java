/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.aggregation.AggregationFactory;
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
  private final MeterProviderSharedState providerSharedState =
      MeterProviderSharedState.create(TestClock.create(), Resource.getEmpty());
  private final MeterSharedState meterSharedState =
      MeterSharedState.create(InstrumentationLibraryInfo.create("test", "1.0"));

  @Test
  void sameAggregator_ForSameLabelSet() {
    SynchronousInstrumentAccumulator<?> accumulator =
        new SynchronousInstrumentAccumulator<>(
            InstrumentProcessor.getCumulativeAllLabels(
                DESCRIPTOR,
                providerSharedState,
                meterSharedState,
                AggregationFactory.count().create(DESCRIPTOR.getValueType())),
            ImmutableSet.of());
    AggregatorHandle<?> aggregatorHandle =
        accumulator.bind(Labels.of("K", "V"), mock(AbstractSynchronousInstrument.class));
    AggregatorHandle<?> duplicateAggregatorHandle =
        accumulator.bind(Labels.of("K", "V"), mock(AbstractSynchronousInstrument.class));
    try {
      assertThat(duplicateAggregatorHandle).isSameAs(aggregatorHandle);
      accumulator.collectAll();
      AggregatorHandle<?> anotherDuplicateAggregatorHandle =
          accumulator.bind(Labels.of("K", "V"), mock(AbstractSynchronousInstrument.class));
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
}
