package trivy

import data.lib.trivy

default ignore = false

ignore_cves := {}

ignore {
  input.VulnerabilityID == ignore_cves[_]
}

ignore_bouncycastle_in_spotless {
  # Only used by spotless in build locally, could not target the spotless source here,
  # but trying to be as specific as possible.
  input.VulnerabilityID == "CVE-2023-33201"
  input.PkgName == "org.bouncycastle:bcprov-jdk18on"
  input.InstalledVersion == "1.72"
}

ignore_jxpath_in_spotless {
  # Only used by spotless in build locally, could not target the spotless source here,
  # but trying to be as specific as possible.
  input.VulnerabilityID == {"CVE-2022-40159","CVE-2022-40160"}[_]
  input.PkgName == "commons-jxpath:commons-jxpath"
  input.InstalledVersion == "1.3"
}

ignore {
  ignore_bouncycastle_in_spotless
}

ignore {
  ignore_jxpath_in_spotless
}
