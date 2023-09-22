/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.client;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/** An example service using and implementing the WebClients */
@Service
public class MyService {

  private final WebClient platformClient;

  private final EmployeeClient employeeClient;

  @Autowired
  public MyService(WebClient platformClient, EmployeeClient employeeClient) {
    this.platformClient = platformClient;
    this.employeeClient = employeeClient;
  }

  public Mono<Employee> createEmployee(Employee employee) {

    return platformClient
        .post()
        .uri("/employees")
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(Mono.just(employee), Employee.class)
        .retrieve() // since it uses reactive methods, we need to handle that in a synchronous
        // way
        .bodyToMono(Employee.class);
  }

  public Mono<Employee> fetchEmployeeById(int id) {
    return platformClient
        .get()
        .uri("/employees/{id}", id)
        .exchangeToMono(
            this::handleResponse); // since it uses reactive methods, we need to handle that in a
    // synchronous way
  }

  public List<Employee> fetchAllEmployees() {
    return employeeClient
        .getAll()
        .block(); // since it uses reactive methods, we need to handle that in a synchronous way
  }

  /**
   * You can define a default method also in the webClient to handle the response and this is an
   * example on how to ovewrite it
   *
   * @param response
   * @return a reactive stream containing an Employee
   */
  private Mono<Employee> handleResponse(ClientResponse response) {

    if (response.statusCode().is2xxSuccessful()) {
      return response.bodyToMono(Employee.class);
    } else if (response.statusCode().is4xxClientError()) {
      // Handle client errors (e.g., 404 Not Found)
      return Mono.error(new Exception("Employee not found"));
    } else if (response.statusCode().is5xxServerError()) {
      // Handle server errors (e.g., 500 Internal Server Error)
      return Mono.error(new RuntimeException("Server error"));
    } else {
      // Handle other status codes as needed
      return Mono.error(new RuntimeException("Unexpected error"));
    }
  }
}
