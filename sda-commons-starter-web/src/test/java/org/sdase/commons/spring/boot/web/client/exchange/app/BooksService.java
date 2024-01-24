package org.sdase.commons.spring.boot.web.client.exchange.app;

import java.util.List;
import org.sdase.commons.spring.boot.web.client.exchange.SdaWebClient;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

@SdaWebClient
@HttpExchange(url = "http://localhost:8090/api")
public interface BooksService {

  @GetExchange("/books")
  Mono<List<Book>> getBooksReactive();


  @GetExchange("/books")
  List<Book> getBooksBlocking();
}
