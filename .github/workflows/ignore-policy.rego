package trivy

import data.lib.trivy

default ignore = false

ignore_cves := {
  # Netty is affected if hostnames are not verified; Netty can be used as asynchronous HTTP client
  # by S3. Since we use the default synchronous HTTP client, we are not affected.
  # Sources
  # - https://github.com/jeremylong/DependencyCheck/issues/5912
  # - https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/http-configuration.html
  "CVE-2023-4586",
  # Logback CVE; Spring Boot is not affected. See https://github.com/spring-projects/spring-boot/issues/38643
  "CVE-2023-6378",
  # json-path is only used sda-commons-web-testing (probably for Wiremock but not clear from the dependency tree)
  "CVE-2023-51074"
}

ignore {
  input.VulnerabilityID == ignore_cves[_]
}
