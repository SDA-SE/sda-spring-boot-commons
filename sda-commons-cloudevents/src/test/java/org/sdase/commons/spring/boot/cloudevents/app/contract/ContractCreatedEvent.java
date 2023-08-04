/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.cloudevents.app.contract;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import org.sdase.commons.spring.boot.cloudevents.CloudEventV1;

public class ContractCreatedEvent extends CloudEventV1<ContractCreatedEvent.ContractCreated> {

  @Schema(
      defaultValue =
          "/SDA-SE/insurance-contract/insurance-contract-stack/insurance-contract-service")
  @Override
  public URI getSource() {
    return super.getSource();
  }

  @Schema(defaultValue = "com.sdase.contract.foo.contract.created")
  @Override
  public String getType() {
    return super.getType();
  }

  @JsonClassDescription("The data of the contract created event.")
  @SuppressWarnings("unused")
  public static class ContractCreated {

    private String contractId;

    private String partnerId;

    public ContractCreated() {}

    public ContractCreated(String contractId, String partnerId) {
      this.contractId = contractId;
      this.partnerId = partnerId;
    }

    public String getContractId() {
      return contractId;
    }

    public ContractCreated setContractId(String contractId) {
      this.contractId = contractId;
      return this;
    }

    public String getPartnerId() {
      return partnerId;
    }

    public ContractCreated setPartnerId(String partnerId) {
      this.partnerId = partnerId;
      return this;
    }
  }
}
