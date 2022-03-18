/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.monitoring.testing;

import org.sdase.commons.spring.boot.starter.monitoring.EnableSdaMonitoring;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableSdaMonitoring
@EnableFeignClients
public class MonitoringTestApp {
  public static void main(String[] args) {
    SpringApplication.run(MonitoringTestApp.class, args);
  }
}
