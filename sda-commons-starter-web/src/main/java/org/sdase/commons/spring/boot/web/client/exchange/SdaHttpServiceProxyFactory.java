package org.sdase.commons.spring.boot.web.client.exchange;

import org.sdase.commons.spring.boot.web.client.exchange.filter.LoggingExchangeFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Component("sdaHttpServiceProxyFactory")
public class SdaHttpServiceProxyFactory {

  @Autowired
  private LoggingExchangeFilter loggingExchangeFilter;

  public Object createProxy(Class<?> clazz) {
    HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(
        WebClientAdapter.create(getWebclient())).build();

    return httpServiceProxyFactory.createClient(clazz);
  }

  private WebClient getWebclient() {
    return WebClient.builder().filter(loggingExchangeFilter).build();
  }

}
