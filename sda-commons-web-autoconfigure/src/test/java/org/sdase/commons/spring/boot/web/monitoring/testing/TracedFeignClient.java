/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.web.monitoring.testing;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import feign.jaxrs2.JAXRS2Contract;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
    value = "testClient",
    url = "${test.tracing.client.base.url}",
    configuration = JAXRS2Contract.class)
public interface TracedFeignClient {

  @GET
  @Path("/pongMetrics")
  @Produces(TEXT_PLAIN)
  String pong();
}
