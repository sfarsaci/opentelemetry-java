/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import java.util.HashMap;

public class ReadWriteLabels extends HashMap<String, String> {
  private static final long serialVersionUID = 362498820763181265L;

  public ReadWriteLabels(Labels labels) {
    super();
    labels.forEach(this::put);
  }

  /** Returns the current object converted to {@link Labels}. */
  public Labels toLabels() {
    LabelsBuilder b = Labels.builder();
    this.entrySet().stream().forEach(e -> b.put(e.getKey(), e.getValue()));
    return b.build();
  }
}
