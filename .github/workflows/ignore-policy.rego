package trivy

import data.lib.trivy

default ignore = false

ignore_cves := {
  "CVE-2022-1471", # org.yaml:snakeyaml.1.33
  "CVE-2016-1000027" # org.springframework:spring-web:5.3.29
}

ignore {
  input.VulnerabilityID == ignore_cves[_]
}
