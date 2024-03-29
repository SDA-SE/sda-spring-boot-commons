# Migration Guide for Upgrading from 2.x.x to 3.x.x

SDA Spring Boot Commons 3 updates 

- Spring Boot from 2.7.x to 3.1.x and 
- Spring Cloud from 2021.x.x to 2022.x.x.

This comes with some breaking changes introduced by Spring and some from SDA Spring Boot Commons as
well.
Other libraries are upgraded or replaced as well.

In addition to this migration guide, the [official migration guide of Spring Boot](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
should be consulted.

Summary of noticeable changes:

- `javax` dependencies are replaced by `jakarta` dependencies.
- Tracing moved from Sleuth to Open Telemetry.
- AsyncAPI setup has been changed, as well as the used library to generate Json Schemas from code.
  Using of the generator is simpler now as [described in its documentation](asyncapi.md).
- A major upgrade of Spring Security is included.
- Spring configuration properties changed.
- S3 libraries changed to a new and different Java API of AWS.
- WireMock dependency changed to _standalone_ variant that provides transitive dependencies
  directly in new packages.


## Compile task

Spring Boot 3 requires reflection information about parameters in compiled classes.
If no such information is added, warning logs will complain about it.
To avoid warnings, the `-parameters` flag must be added to the compile task.

=== "Gradle"
    ```groovy
    tasks.withType(JavaCompile).configureEach {
      options.encoding = 'UTF-8'
      options.compilerArgs << '-parameters'
    }
    ```
=== "Maven"
    ```xml
    <build>
      <plugins>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-compiler-plugin</artifactId>
          <configuration>
            <compilerArgs>
              <arg>-parameters</arg>
            </compilerArgs>
          </configuration>
        </plugin>
      </plugins>
    </build>
    ```


## Jakarta EE

Please make sure dependencies do not pull in transitive `javax` modules and migrate all `javax`
imports to `jakarta`.
The provided dependency management should take care about all dependencies by referring to the
dependency management of Spring Boot as mentioned in [the migration guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide#jakarta-ee).

Please note that Wiremock repackaged `javax` classes.
They will be available in tests but should not be used in your code.

## Feign Clients with JAX-RS

If Feign Clients are declared with JAX-RS annotations like `@POST` and the
`feign.jaxrs2.JAXRS2Contract` configuration, it must be changed

- to Jakarta annotations and the `feign.jaxrs.JakartaContract` configuration
- or to Spring Boot annotations.


## Tracing

Tracing uses OpenTelemetry now.
Any configuration that references `zipkin` or `sleuth` is not considered anymore, such as
environment variables `JAEGER_ENABLED` `SPRING_ZIPKIN_BASE_URL` and `SPRING_ZIPKIN_ENABLED`.

The environment variable `JAEGER_SERVICE_NAME` was replaced by the spring's default system
property `spring.application.name`.

Please refer to the respective `management.tracing` and other migrated properties in
[the documentation](starter-web.md#configuration).


## AsyncAPI generation

Json Schemas for AsyncAPI are generated with
[Victools' Json Schema Generator](https://github.com/victools/jsonschema-generator) now.
The [previously used library](https://github.com/mbknor/mbknor-jackson-jsonSchema) is barely
maintained in the past years.

The old library provided their own annotations.
Now, annotations of Jackson (e.g. `@JsonSchemaDescription`), Swagger (e.g. `@Schema`) and Jakarta
Validation (e.g. `NotNull`) can be used.
Note that not all attributes of all annotations are covered and multiple examples are not possible
anymore.
Only one example can be defined with `@Schema(example = "value")`.

How the Java classes for schema definitions in the AsyncAPI are defined has changed.
Previously, classes to integrate were defined in the code
(`.withSchema("./schema.json", BaseEvent.class)`) and referenced in the AsyncAPI template
(`$ref: './schema.json#/definitions/CarManufactured'`).
Now the classes are referenced directly in the template (`$ref: 'class://com.example.BaseEvent`).
The builder method `withSchema` does not exist any more.

Please review the differences in the generated AsyncAPI file.
Both libraries work different and have a different feature set.
The new generator may have some limitations but a great API for extensions.
Please [file an issue](https://github.com/SDA-SE/sda-spring-boot-commons/issues) if something
important can't be expressed.


## MongoDB

If you use `de.flapdoodle.embed:de.flapdoodle.embed.mongo` for testing, change to
`de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring3x`.
To define the database version used for tests, use the property
`de.flapdoodle.mongodb.embedded.version` instead of `spring.mongodb.embedded.version`


## S3

The simple interface
client [`com.amazonaws.services.s3.AmazonS3`](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/s3/AmazonS3.html) (
Java 1.x), available on version 2.x, was
replaced by the service
client [`software.amazon.awssdk.services.s3.S3Client`](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/services/s3/S3Client.html) (
Java 2.x) in this version, so you
should replace your autowired beans with this new service client and its methods.
If you define this bean with custom configuration you need to update your configuration, e.g.:

```diff
 @Configuration
 public class Foo {
-  private AmazonS3 createClientWithCustomConfiguration() {
-    final AWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretKey);
-    return AmazonS3ClientBuilder.standard()
-        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
-        .withPathStyleAccessEnabled(true)
-        .withCredentials(new AWSStaticCredentialsProvider(credentials))
+  private S3Client createClientWithCustomConfiguration() {
+    return S3Client.builder()
+        .region(Region.of(region))
+        .credentialsProvider(
+            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretKey)))
+        .endpointProvider(S3EndpointProvider.defaultProvider())
+        .endpointOverride(URI.create(endpoint))
+        .forcePathStyle(true)
         .build();
   }
 }

```

You can check the new API
definition [here](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/services/s3/S3Client.html).


## Kafka

There has been introduced a new default value for the `sda.kafka.consumer.dlt.pattern` property,
which is `dlt-<topic>`. If you want to keep the old behaviour, which is appending `.DLT` to the
consumed topic, you have to
explicitly unset the property (`sda.kafka.consumer.dlt.pattern=`).

!!! Info
We do not recommend using a '.' within the topic name, since it does not allow the standard
replacement approach in our kustomize deployments and let to errors in customer's environments.


## Testing - WireMock

WireMock changed from `wiremock-jre8` to `wiremock-jre8-standalone` as
[suggested here](https://github.com/wiremock/wiremock/issues/1760).
The standalone variant repackages transitive dependencies to a new package and avoids the
dependency.
Tests are affected if Jackson classes are used to interact with WireMock, e.g.
`.willReturn(jsonResponse(jacksonsJsonNode, 200))`.
All imports related to Jackson classes interacting with WireMock, including an `ObjectMapper` that
is used to read or write these objects, must be updated from `com.fasterxml.…` to
`wiremock.com.fasterxml.…`.
