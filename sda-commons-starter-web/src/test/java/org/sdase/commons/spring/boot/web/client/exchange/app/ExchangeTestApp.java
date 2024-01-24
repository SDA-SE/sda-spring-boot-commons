package org.sdase.commons.spring.boot.web.client.exchange.app;

import org.sdase.commons.spring.boot.web.client.exchange.EnableSdaWebClient;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.sdase.commons.spring.boot.web.client.exchange")
@EnableSdaWebClient(basePackages = "org.sdase.commons.spring.boot.web.client.exchange")
public class ExchangeTestApp {

}
