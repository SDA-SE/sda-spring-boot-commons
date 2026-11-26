# sda-spring-boot-commons
[![Latest Release](https://img.shields.io/github/v/release/sda-se/sda-spring-boot-commons?label=latest)](https://github.com/SDA-SE/sda-spring-boot-commons/releases/latest)
[![Maven Central Version](https://img.shields.io/maven-central/v/org.sdase.commons.spring.boot/sda-commons-starter-web)](https://central.sonatype.com/namespace/org.sdase.commons.spring.boot)
[![Java CI](https://github.com/SDA-SE/sda-spring-boot-commons/actions/workflows/java-ci.yaml/badge.svg)](https://github.com/SDA-SE/sda-spring-boot-commons/actions/workflows/java-ci.yaml)

![spring](https://user-images.githubusercontent.com/61695677/155694976-dc7f9376-32ef-4be0-a919-3cc41a1f0341.png)

A set of libraries to bootstrap spring boot services easily that follow the patterns and
specifications promoted by the SDA SE.

> 🥳 Upgrade to Spring Boot 4 is released with [version 7.0.0](https://github.com/SDA-SE/sda-spring-boot-commons/releases/tag/7.0.0). 🥳
>
> Please follow [the migration guide](./docs/migration-6-to-7.md) to upgrade.
> 
> We will still provide security patches for the Spring Boot 3 based version. However, we recommend
> upgrading to Spring Boot 4 as soon as possible.

## Getting Started

Please have a look in our [documentation](docs/index.md).


## Changelog and Versioning

This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

See our [changelog](https://github.com/SDA-SE/sda-spring-boot-commons/releases/) for more information about the latest features.


### Releasing

After the build passes for a push to `main`, Semantic Release checks the commits since the last
release. If they call for a new version, it prepares a GitHub draft with the proposed version and
release notes.

1. Open the draft targeting `main` and check its version and notes against the commits you intend
   to ship. An earlier patch draft can still be visible after a feature commit produces a minor
   draft. After publishing a minor release, check for remaining patch drafts and remove them manually.
   [GitHub Releases](https://github.com/SDA-SE/sda-spring-boot-commons/releases)
2. While the draft is open, a new run can replace it and overwrite manual edits. Make your final
   edits, then choose **Publish release**.
3. Check the Maven Central workflow run for that tag. The `build-and-test` job reruns Java CI on
   the tagged commit. Once it passes, `upload-maven-central` signs and uploads the artifacts.
   [Publish Release to Maven Central](https://github.com/SDA-SE/sda-spring-boot-commons/actions/workflows/publish-release-maven-central.yml)


## Contributing

We are looking forward to contributions.
Take a look at our [Contribution Guidelines](./CONTRIBUTING.md) before submitting Pull Requests.


## Responsible Disclosure and Security

The [SECURITY.md](./SECURITY.md) includes information on responsible disclosure and security related topics like security patches.
