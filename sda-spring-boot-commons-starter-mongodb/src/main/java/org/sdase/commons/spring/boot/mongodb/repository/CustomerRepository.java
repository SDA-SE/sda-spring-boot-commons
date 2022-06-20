package org.sdase.commons.spring.boot.mongodb.repository;

import org.sdase.commons.spring.boot.mongodb.Entity.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;



public interface CustomerRepository extends MongoRepository<Customer, String> {
}
