# SDA Commons BOM

The module `sda-commons-bom` defines the versions of sda-commons modules.

## Usage

Services using sda-commons can import `sda-commons-bom` to make sure to use the same
dependencies. After that you can use all modules of sda-commons **without** declaring the version.
Example for your `build.gradle`:

```
dependencies {
    implementation enforcedPlatform("org.sdase.commons.spring.boot:sda-commons-bom:$sdaSpringCommonsVersion")
    implementation enforcedPlatform("org.sdase.commons.spring.boot:sda-commons-dependencies:$sdaSpringCommonsVersion")
    implementation "org.sdase.commons.spring.boot:sda-commons-starter-web"
    ...
}
```
