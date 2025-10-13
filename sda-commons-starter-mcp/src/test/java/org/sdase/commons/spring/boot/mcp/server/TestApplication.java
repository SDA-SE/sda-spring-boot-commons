/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.server;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@SpringBootApplication
public class TestApplication {

  public static void main(String[] args) {
    SpringApplication.run(TestApplication.class, args);
  }

  @Bean
  public ToolCallbackProvider helloTools(HelloTool helloTool) {
    return MethodToolCallbackProvider.builder().toolObjects(helloTool).build();
  }
}

@Service
class HelloTool {

  @Tool(description = "Say hello to a given name or to the world, if no name is passed.")
  public String sayHello(String name) {

    return (name == null || name.isBlank()) ? "Hello, World!" : "Hello, " + name + "!";
  }
}
