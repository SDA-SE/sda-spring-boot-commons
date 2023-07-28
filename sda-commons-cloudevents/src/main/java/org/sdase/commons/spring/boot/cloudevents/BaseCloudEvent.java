/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.cloudevents;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import jakarta.validation.constraints.NotEmpty;

public abstract class BaseCloudEvent {

  @JsonPropertyDescription(
      "The version of the CloudEvents specification which the event uses. "
          + "This enables the interpretation of the context. Compliant event producers MUST use a "
          + "value of `1.0` when referring to this version of the specification.\n"
          + "\n"
          + "Currently, this attribute will only have the 'major' and 'minor' version numbers "
          + "included in it. This allows for 'patch' changes to the specification to be made without "
          + "changing this property's value in the serialization. Note: for 'release candidate' "
          + "releases a suffix might be used for testing purposes.")
  @JsonSchemaExamples("1.0")
  @NotEmpty
  private String specversion;

  public String getSpecversion() {
    return specversion;
  }

  public BaseCloudEvent setSpecversion(String specversion) {
    this.specversion = specversion;
    return this;
  }
}
