/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.cloudevents;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.asyncapi.AsyncApiGenerator;
import org.sdase.commons.spring.boot.cloudevents.app.KafkaTestApp;
import org.sdase.commons.spring.boot.cloudevents.app.contract.ContractCreatedEvent;
import org.sdase.commons.spring.boot.cloudevents.app.partner.PartnerCreatedEvent;
import org.sdase.commons.spring.boot.web.testing.GoldenFileAssertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {KafkaTestApp.class})
class AsyncApiDocumentationTest {

  @Autowired private AsyncApiGenerator asyncApiGenerator;

  @Test
  void generateAndVerifySpec() throws IOException {

    String expected =
        asyncApiGenerator
            .builder()
            .withAsyncApiBase(getClass().getResource("/asyncapi-template.yml"))
            .withSchema("/PartnerCreatedEvent", PartnerCreatedEvent.class)
            .withSchema("/ContractCreatedEvent", ContractCreatedEvent.class)
            .withSchema("/PlainCloudEvent", CloudEventV1.class)
            .generateYaml();

    // specify where you want your file to be stored
    Path filePath = Paths.get("asyncapi.yaml");

    // check and update the file
    GoldenFileAssertions.assertThat(filePath).hasYamlContentAndUpdateGolden(expected);
  }
}
