/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

/**
 * Enables support for {@link org.springframework.cloud.openfeign.FeignClient}s that support SDA
 * Platform features like
 *
 * <ul>
 *   <li>passing the Authorization header to downstream services.
 *   <li>OIDC client authentication.
 * </ul>
 *
 * <p>A feign client can be created as interface like this:
 *
 * <pre>
 *   <code>{@literal @}FeignClient(name = "partnerOds", url = "${partnerOds.baseUrl}")
 *    public interface OtherServiceClient {
 *      {@literal @}GetMapping("/partners")
 *      List&lt;Partner&gt; getPartners();
 *    }
 *   </code>
 * </pre>
 *
 * <p>The client is then available as bean in the Spring context.
 *
 * <p>The Partner ODS base url must be configured as {@code http://partner-ods:8080/api} in the
 * Spring environment property {@code partnerOds.baseUrl}. Detailed configuration like timeouts can
 * be configured with <a
 * href="https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/#spring-cloud-feign-overriding-defaults">default
 * feign properties</a> in the {@code application.yaml} or derived environment properties based on
 * the {@code name} attribute of the {@link org.springframework.cloud.openfeign.FeignClient}
 * annotation.
 *
 * <p>The client can be used within the SDA Platform to path through the received authentication
 * header by adding a configuration:
 *
 * <pre>
 *   <code>{@literal @}FeignClient(
 *       name = "partnerOds",
 *       url = "${partnerOds.baseUrl}",
 *       configuration = {AuthenticationPassThroughClientConfiguration.class})
 *    public interface OtherServiceClient {
 *      {@literal @}GetMapping("/partners")
 *      List&lt;Partner&gt; getPartners();
 *    }
 *   </code>
 * </pre>
 *
 * {@link AuthenticationPassThroughClientConfiguration} will take the {@code Authorization} header
 * from the current request context of the servlet and adds its value to the client request.
 *
 * <p>If the request context is not always existing, e.g. in cases where a technical user for
 * service-to-service communication is required, the {@link OidcClientConfiguration} will request
 * the required OIDC authentication token with the client credentials flow using the configured
 * {@code "oidc.client.issuer.uri"}, {@code "oidc.client.id"} and {@code "oidc.client.secret"}. If
 * the current request context contains the {@code Authorization} header, the authentication
 * pass-through will be applied instead.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@EnableFeignClients
@Import({SdaClientConfiguration.class, SdaOidcClientConfiguration.class})
public @interface EnableSdaClients {}
