/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.cloudevents.app.partner;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import org.sdase.commons.spring.boot.cloudevents.CloudEventV1;

@JsonClassDescription("An event that is published when a partner has been created.")
public class PartnerCreatedEvent extends CloudEventV1<PartnerCreatedEvent.PartnerCreated> {

  private static final String DEFAULT_SOURCE = "/SDA-SE/partner/partner-stack/partner-service";
  private static final String DEFAULT_TYPE = "com.sdase.partner.ods.partner.created";

  public PartnerCreatedEvent() {
    super();
    super.setSource(URI.create(DEFAULT_SOURCE));
    super.setType(DEFAULT_TYPE);
  }

  @JsonClassDescription("Details about the created partner.")
  public record PartnerCreated(
      @Schema(
              description = "The unique id of a partner.",
              example = "FF427BC8-B38F-43CC-8AAB-512843808A18")
          String id) {}
}
