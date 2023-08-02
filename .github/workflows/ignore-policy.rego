package trivy

import data.lib.trivy

default ignore = false

ignore_cves := {
  # org.yaml:snakeyaml.1.33
  "CVE-2022-1471",
  # com.amazonaws:aws-java-sdk-s3 transitive test dependency of io.findify:s3mock_2.13
  "CVE-2022-31159",
}

ignore {
  input.VulnerabilityID == ignore_cves[_]
}
