/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.mongodb.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableSdaMongoDb
public class MongoTestApp {
  public static void main(String[] args) {
    SpringApplication.run(MongoTestApp.class, args);
  }
}
