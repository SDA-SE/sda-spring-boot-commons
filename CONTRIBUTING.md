# Contribution Guidelines

We are looking forward to your contributions.
Please take a moment to read these Contribution Guidelines.


## Submitting Pull Requests

In case you want to submit a small fix, feel free to create a Pull Request.
If you plan bigger changes, prefer to create an issue first to discuss the details.
Make sure that your changes can be build and run on Linux, Mac and Windows.
Your changes should also contain tests.
Make sure that the coverage of new code is above 80%.
Avoid breaking changes!

> **Some checks are unavailable for external Pull Requests from forks.** 
>
> Maintainers have to run these checks manually.

### Code Formatting

This project adheres to the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).
You can use `./gradlew spotlessApply` to format your code according to the style guide.

### Changelog and Versioning

This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html). 
Make sure that your commit messages follows the [Semantic Commits](https://gist.github.com/stephenparish/9941e89d80e2bc58a153)
conventions.
Some examples for semantic commits:
* `feat(starter-web): add new key source` - for new features inside the starter-web module,
  creates a new minor release.
* `fix(starter-web): make key source work on Palm devices` - for fixes to existing features inside
  the starter-web module, creates a new patch release.
* `test(starter-kafka): improve reliability for Kafka tests` - for new or modified tests, no release
  is created.
* `docs: add a CONTRIBUTING.md` - for changes that only modify documentation, no release is created.
* `chore: improve build speed` - for changes to everything else, no release is created.

Our [changelog](https://github.com/SDA-SE/sda-spring-boot-commons/releases/) is maintained in the
GitHub releases.


### PR Snapshots

> **PR Snapshots are unavailable for Pull Requests from forks!**

Each PR creates a snapshot that can _temporarily_ be included in other projects for testing.
The generated version uses the format: `PR-<pr_number>-SNAPSHOT`.
Snapshots are cleaned up regularly from the repository so never use snapshots in stable releases.
The snapshots are currently hosted in our internal Nexus repository.

Import snapshots by adding the snapshot repository to the `build.gradle`:

```gradle
    repositories {
      ...
      maven {
        url "https://nexus.sda-se.io/repository/sda-se-snapshots/"
        credentials {
          username sdaNexusUser
          password sdaNexusPassword
        }
      }
      ...
    }
```
