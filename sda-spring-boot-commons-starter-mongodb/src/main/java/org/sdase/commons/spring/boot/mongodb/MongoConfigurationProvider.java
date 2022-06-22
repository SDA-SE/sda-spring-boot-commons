package org.sdase.commons.spring.boot.mongodb;

import org.springframework.context.annotation.Configuration;
import java.util.function.Function;

@FunctionalInterface
public interface MongoConfigurationProvider <C extends Configuration>
    extends Function<C, MongoConfiguration> {}
