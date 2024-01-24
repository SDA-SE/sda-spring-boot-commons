package org.sdase.commons.spring.boot.web.client.exchange.app;

import org.sdase.commons.spring.boot.web.client.exchange.SdaWebClient;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

@SdaWebClient
@HttpExchange(url = "https://catfact.ninja/")
public interface FactService {

  @GetExchange(url = "/fact")
  Mono<Fact> getFact();

}
