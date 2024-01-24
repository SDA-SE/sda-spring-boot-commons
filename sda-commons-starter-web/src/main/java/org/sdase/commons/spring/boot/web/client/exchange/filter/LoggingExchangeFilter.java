package org.sdase.commons.spring.boot.web.client.exchange.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@Component
public class LoggingExchangeFilter implements ExchangeFilterFunction {

  private static final Logger LOG = LoggerFactory.getLogger(LoggingExchangeFilter.class);

  @Override
  public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {

    LOG.info(request.url().getPath());
    return next.exchange(request);
  }
}
