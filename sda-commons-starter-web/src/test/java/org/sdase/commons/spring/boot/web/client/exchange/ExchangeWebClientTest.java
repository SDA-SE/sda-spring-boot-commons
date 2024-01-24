package org.sdase.commons.spring.boot.web.client.exchange;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.client.exchange.app.Book;
import org.sdase.commons.spring.boot.web.client.exchange.app.BooksService;
import org.sdase.commons.spring.boot.web.client.exchange.app.ExchangeTestApp;
import org.sdase.commons.spring.boot.web.client.exchange.app.Fact;
import org.sdase.commons.spring.boot.web.client.exchange.app.FactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

@SpringBootTest(classes = ExchangeTestApp.class)
@AutoConfigureWireMock(port = 8090)
public class ExchangeWebClientTest {

  @Autowired
  private BooksService booksService;

  @Autowired
  private FactService factService;

  @Test
  void exchangeUsesSdaWebClientTest() {

    WireMock.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/api/books"))
            .willReturn(ResponseDefinitionBuilder.okForJson(List.of(new Book("Book 1", "Author 1"), new Book("Book 2", "Author 2")))));

    Fact facts = factService.getFact().block();
    System.out.println(facts);

    List<Book> booksBlocking = booksService.getBooksBlocking();
    System.out.println(booksBlocking);
  }
}
