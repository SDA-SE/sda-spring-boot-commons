package trivy

import data.lib.trivy

default ignore = false

# add CVE as String and a comment why it is ignored
ignore_cves := {
}

ignore {
  input.VulnerabilityID == ignore_cves[_]
}
