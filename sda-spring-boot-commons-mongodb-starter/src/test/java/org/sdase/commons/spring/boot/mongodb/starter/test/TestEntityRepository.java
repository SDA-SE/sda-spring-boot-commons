/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.mongodb.starter.test;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface TestEntityRepository extends MongoRepository<TestEntity, String> {}
