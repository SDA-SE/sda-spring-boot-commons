# Security Hardening

sda-spring-boot-commons changes some default configuration for security reasons.
This document provides a brief overview about the addressed risks.

## Risk: Accessing critical resources from untrusted environments

To avoid exposing internal resources, Spring Boot Actuator is configured to listen on a separate
port.
Health, metrics and other sensitive information can't be exposed to the internet by accident, e.g.
by missing to exclude the actuator path.

Custom critical resources can be exposed at the management port by implementing
`org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint` or
`org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpoint`.
Note that there is an [open discussion](https://github.com/spring-projects/spring-boot/issues/31768)
about these annotations.
As long as they are not deprecated, it is suggested to use them because the use is most similar to
controllers used in regular REST APIs.

## Risk: Root start

If the service is started with extended privileges as the root user, an attacker can more easily
attack the operating system after taking over from the container.

The default configuration is capable to run as no root, listening to ports 8080 and 8081.
Deployment checks must ensure, that the container is not configured with a root user.

## Risk: Exploitation of HTTP methods

The HTTP method `TRACE` is disabled by default to mitigate [Cross Site Tracing](https://owasp.org/www-community/attacks/Cross_Site_Tracing).

## Risk: Loss of source IP address

We expect, that services built with sda-spring-boot-commons are deployed behind a proxy, e.g. an
Ingress in Kubernetes.

This library is configured by default to consider `X-Forwarded-*` headers to identify the original
caller.

## Risk: Detection of confidential components

Knowing the components used in a software makes it easier to look for and exploit specific CVEs.

Custom error handlers and other configurations are used to avoid identifiable default output from
the framework and its components.

## Risk: Lack of visibility

If there is no visibility, there is no response to an abusive action and attackers can explore risks
undisturbed.

Logs are written to standard out by default to comply with Kubernetes environments.
Prometheus metrics are exposed as expected by SDA environments.

## Risk: Buffer Overflow

- The size of request and response headers is limited to 8KiB.
- The size of a request body is limited to 1 MB by default, chunked encoding is not accepted and the
  `Content-Length` request header is required. The limit [can be changed](../web/index.md#configuration).

## Header

By configuring the default headers, the following risks are addressed:

- Cross-Site Scripting
- Content interpretation by the browser
- Content loading in Flash and PDFs
- Clickjacking
- Sharing visited URLs with third parties
- Abuse from Cross-Origin Resource Sharing
