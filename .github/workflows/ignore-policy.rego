package trivy

import data.lib.trivy

default ignore = false

ignore_cves := {
  # "CVE-2016-1000027", # description
}

ignore {
  input.VulnerabilityID == ignore_cves[_]
}
