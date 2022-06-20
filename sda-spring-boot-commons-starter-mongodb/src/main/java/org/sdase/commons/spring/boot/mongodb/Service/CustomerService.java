package org.sdase.commons.spring.boot.mongodb.Service;

import org.sdase.commons.spring.boot.mongodb.repository.CustomerRepository;
import org.springframework.data.mongodb.core.MongoTemplate;

public class CustomerService {

  private final MongoTemplate mongoTemplate;
  private CustomerRepository customerRepository;

  public CustomerService(MongoTemplate mongoTemplate, CustomerRepository customerRepository) {
    this.mongoTemplate = mongoTemplate;
    this.customerRepository = customerRepository;
  }


}
