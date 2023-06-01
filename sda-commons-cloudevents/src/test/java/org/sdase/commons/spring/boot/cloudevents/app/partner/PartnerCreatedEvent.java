/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.cloudevents.app.partner;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDefault;
import java.net.URI;
import org.sdase.commons.spring.boot.cloudevents.CloudEventV1;

public class PartnerCreatedEvent extends CloudEventV1<PartnerCreatedEvent.PartnerCreated> {

  @JsonSchemaDefault("`/SDA-SE/partner/partner-stack/partner-service`")
  @Override
  public URI getSource() {
    return super.getSource();
  }

  @JsonPropertyDescription("`com.sdase.partner.ods.partner.created`")
  @JsonSchemaDefault("com.sdase.partner.ods.partner.created")
  @Override
  public String getType() {
    return super.getType();
  }

  public static class PartnerCreated {
    private String id;

    public String getId() {
      return id;
    }

    public PartnerCreated setId(String id) {
      this.id = id;
      return this;
    }
  }
}
