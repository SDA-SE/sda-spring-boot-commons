/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.monitoring.testing;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

import feign.jaxrs.JakartaContract;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
    value = "testClient",
    url = "${test.tracing.client.base.url}",
    configuration = JakartaContract.class)
public interface TracedFeignClient {

  @GET
  @Path("/pongMetrics")
  @Produces(TEXT_PLAIN)
  String pong();
}
