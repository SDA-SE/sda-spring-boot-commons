package trivy

import data.lib.trivy

default ignore = false

ignore_cves := {
  "CVE-2022-40159", # commons-jxpath:commons-jxpath:1.3: used by spotless-2116170985 (strange name, you can find it if you execute `./gradlew dependencies`), i.e. only build-time dependency
  "CVE-2022-40160", # commons-jxpath:commons-jxpath:1.3: used by spotless-2116170985 (strange name, you can find it if you execute `./gradlew dependencies`), i.e. only build-time dependency
  "CVE-2023-42503", # org.apache.commons:commons-compress:1.23.0: Only used by Flapdoodle for tests
}

ignore {
  input.VulnerabilityID == ignore_cves[_]
}
