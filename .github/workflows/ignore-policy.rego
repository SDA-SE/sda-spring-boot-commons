package trivy

import data.lib.trivy

default ignore = false

ignore_cves := {
  # lz4-java is only used by Kafka. And they staid there code is not affected under Linux, besides some corner cases.
  # https://issues.apache.org/jira/browse/KAFKA-19951?focusedCommentId=18042357&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-18042357
  "CVE-2025-12183",
  "CVE-2025-66566",
  # It's about a test dependency and afaik related to XML which we don't use.
  "CVE-2026-24400"
}

ignore {
  input.VulnerabilityID == ignore_cves[_]
}
