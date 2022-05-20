/*
 * Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
 *
 * All rights reserved.
 */
package org.sdase.commons.spring.boot.web.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;
import org.sdase.commons.spring.boot.web.auth.opa.AbstractConstraints;
import org.sdase.commons.spring.boot.web.auth.opa.Constraints;
import org.sdase.commons.spring.boot.web.auth.opa.OpaAccessDecisionVoter;
import org.sdase.commons.spring.boot.web.auth.opa.OpaRequestBuilder;
import org.sdase.commons.spring.boot.web.auth.opa.OpaRestTemplateConfiguration;
import org.sdase.commons.spring.boot.web.auth.opa.extension.OpaInputExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

/**
 * Enables features that make a Spring Boot service compliant with the <a
 * href="https://sda.dev/core-concepts/security-concept/authentication/">SDA SE Authentication</a>
 * and <a href="https://sda.dev/core-concepts/security-concept/authorization/">SDA SE
 * Authorization</a> concepts using OIDC and Open Policy Agent.
 *
 * <p>OIDC Authentication can be configured with {@code AUTH_ISSUERS} to provider a comma separated
 * list of trusted issuers. In develop and test environments, the boolean {@code AUTH_DISABLE} may
 * be used to disable authentication.
 *
 * <p>The JWKS URI of each issuer is updated when an unknown Key ID is received and every 5 minutes.
 * The cache of known JWK is invalidated after 15 minutes.
 *
 * <p><strong>This setup allows authenticated and anonymous requests! It is the responsibility of
 * policies provided by the Open Policy Agent to decide about denying anonymous requests.</strong>
 *
 * <p>Authorization with the Open Policy Agent can be configured as described in {@link
 * OpaAccessDecisionVoter#OpaAccessDecisionVoter(boolean, String, String, OpaRequestBuilder,
 * RestTemplate, ApplicationContext, io.opentracing.Tracer)} and {@link
 * OpaRestTemplateConfiguration#OpaRestTemplateConfiguration(Duration, Duration)}.
 *
 * <p>Constraints provided with the Open Policy Agent response can be mapped to a custom pojo. If
 * the class extends {@link AbstractConstraints} and is annotated with {@link Constraints} it can be
 * {@link org.springframework.beans.factory.annotation.Autowired} in {@link
 * org.springframework.web.servlet.mvc.Controller}s or {@link
 * org.springframework.web.bind.annotation.RestController}s.
 *
 * <p>Additional parameters that are needed for the authorization decision may be provided with
 * custom {@link OpaInputExtension}s.
 *
 * <p>Testing {@code SpringBootTest}s is supported by {@code DisableSdaAuth} and {@code
 * EnableSdaAuthMock}.
 *
 * <p>{@code /openapi.yaml} and {@code /openapi.json} are excluded from authorization requirements.
 * Custom excluded paths can be configured as comma separated list of regex in {@code
 * opa.exclude.patterns}. This will overwrite the default excludes of the OpenAPI documentation
 * paths.
 *
 * <p>Spring Security is disabled for the Management/Admin Port (default: 8081). Be aware that these
 * port should not be accessible out of the deployment context.
 *
 * <p>This security implementation lacks some features compared to <a
 * href="https://github.com/SDA-SE/sda-dropwizard-commons/tree/master/sda-commons-server-auth">sda-dropwizard-commons</a>:
 *
 * <ul>
 *   <li>No configuration of static local public keys to verify the token signature.
 *   <li>No configuration of JWKS URIs to verify the token signature.
 *   <li>The IDP must provide an {@code iss} claim that matches the base URI for discovery.
 *   <li>Leeway is not configurable yet.
 *   <li>The client that loads the JWKS is not configurable yet.
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({OpaRestTemplateConfiguration.class, SdaSecurityConfiguration.class})
public @interface EnableSdaSecurity {}
