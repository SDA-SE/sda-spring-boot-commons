# SDA Commons dependencies

The module `sda-commons-dependencies` defines the versions of sda-commons' dependencies. This
concept is mostly known as
either [platform](https://docs.gradle.org/current/userguide/java_platform_plugin.html)
or "bill of materials".

## Usage

Services using sda-commons should import `sda-commons-dependencies` to make sure to use the same
dependencies. Add the following code to your `build.gradle`:

```
dependencies {
    implementation enforcedPlatform("org.sdase.commons.spring.boot:sda-commons-dependencies:$sdaSpringCommonsVersion")
}
```

After that you can use all dependencies that were already declared in `sda-commons-dependencies`
**without** declaring the version.
