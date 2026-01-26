package trivy
import data.lib.trivy

default ignore := false

# permissive licenses from export of backend definition in Fossa,
# see policy-backend-fossa for reference
default permissive := {
    "0BSD",
    "AFL-3.0", # Permissive license which is perfectly safe to use provided proper attribution is given and retained.
    "android-sdk",
    "Apache-1.1", # Permissive license which is perfectly safe to use provided proper attribution is given and retained.
    "Apache-2.0", # Permissive license which is perfectly safe to use provided proper attribution is given and retained.
    "Artistic-1.0", # Safe if code isn’t modified and notice requirements are followed. Otherwise, you must state and disclose the source code of modifications/derivative works.
    "BouncyCastle",
    "BSD-1-Clause", # Permissive license which is perfectly safe to use provided proper attribution is given and retained.
    "BSD-2-Clause", # Permissive license which is perfectly safe to use provided proper attribution is given and retained.
    "BSD-3-Clause", # Permissive license which is perfectly safe to use provided proper attribution is given and retained.
    "BSD-3-Clause-No-Nuclear-Warranty",
    "BSD-4-Clause", # Permissive license which is perfectly safe to use provided proper attribution is given and retained.
    "CC-BY-2.5",
    "CC-BY-3.0",
    "CC0-1.0",
    "CDDL-1.0", # Safe if code isn’t modified and notice requirements are followed. Otherwise, you must state and disclose the source code of modifications/derivative works.
    "CDDL-1.1",
    "CPL-1.0",
    "EPL-1.0",
    "EPL-2.0",
    "GPL-2.0-with-classpath-exception", # Safe to include or link in an executable provided that source availability/attribution requirements are followed.
    "ICU",
    "ISC", # Permissive license which is perfectly safe to use provided proper attribution is given and retained.
    "JSON",
    "LGPL-2.0-only", # Requires you to (effectively) disclose your source code if the library is statically linked to your project. Not triggered if dynamically linked or a separate process.
    "LGPL-2.0-or-later", # Requires you to (effectively) disclose your source code if the library is statically linked to your project. Not triggered if dynamically linked or a separate process.
    "LGPL-2.1-only", # Requires you to (effectively) disclose your source code if the library is statically linked to your project. Not triggered if dynamically linked or a separate process.
    "LGPL-2.1-or-later", # Requires you to (effectively) disclose your source code if the library is statically linked to your project. Not triggered if dynamically linked or a separate process.
    "LGPL-3.0-only", # Requires you to (effectively) disclose your source code ifthe library is statically linked to your project. Not triggered if dynamically linked or a separate process.
    "LGPL-3.0-or-later", # Requires you to (effectively) disclose your source code ifthe library is statically linked to your project. Not triggered if dynamically linked or a separate process.
    "MIT", # Permissive license which is perfectly safe to use provided proper attribution is given and retained.
    "MPL-1.1", # Safe if code isn’t modified and notice requirements are followed. Otherwise, you must state and disclose the source code of modifications/derivative works.
    "MPL-2.0", # Safe if code isn’t modified and notice requirements are followed. Otherwise, you must state and disclose thesource code of modifications/derivative works.
    "OpenSSL",
    "public-domain",
    "SAX-PD",
    "Unlicense",
    "W3C", # Permissive license which is perfectly safe to use provided proper attribution is given and retained.
    "WTFPL", # Permissive license which is perfectly safe to use provided proper attribution is given and retained.
    "X11",
    "Zlib", # Permissive license which is perfectly safe to use provided proper attribution is given and retained.
  }

# mapping of licenses identified by cyclonedx to known license keys
default licenseMapping := {
    "Unicode/ICU License": "ICU",
    "Bouncy Castle Licence": "BouncyCastle",
    # both licenses are permissive, we pick one
    "(CDDL-1.0 OR GPL-2.0-with-classpath-exception)": "GPL-2.0-with-classpath-exception",
    "Apache License, 2.0": "Apache-2.0",
    "The GNU General Public License (GPL), Version 2, With Classpath Exception": "GPL-2.0-with-classpath-exception",
    "Public Domain": "public-domain",
    "Eclipse Public License (EPL) 2.0": "EPL-2.0",
    "GNU Lesser General Public License": "LGPL",
  }

# default: allow everything defined in the list of permissive licenses
ignore {
  input.Name == permissive[_]
}

# allow licenses that are only named different due to the used tooling
ignore {
  licenseMapping[input.Name] == permissive[_]
}

# false identification, it's Apache 2, see https://github.com/facebook/rocksdb/blob/main/LICENSE.Apache
ignore {
  input.PkgName == "org.rocksdb:rocksdbjni"
  input.Name == "GNU General Public License, version 2"
}

# MIT-0 is even more permissive than MIT, see https://github.com/aws/mit-0
ignore {
  input.PkgName == "org.reactivestreams:reactive-streams"
  input.Name == "MIT-0"
}

# ch.qos.logback:logback-classic is dual licensed as LGPL 2.1 or Eclipse Public License v1.0
# see https://github.com/qos-ch/logback/blob/master/LICENSE.txt
# cyclonedx identifies GNU Lesser General Public License
ignore {
  input.PkgName == "ch.qos.logback:logback-classic"
  input.Name == "GNU Lesser General Public License"
}

# ch.qos.logback:logback-core is dual licensed as LGPL 2.1 or Eclipse Public License v1.0
# see https://github.com/qos-ch/logback/blob/master/LICENSE.txt
# cyclonedx identifies GNU Lesser General Public License
ignore {
  input.PkgName == "ch.qos.logback:logback-core"
  input.Name == "GNU Lesser General Public License"
}

# ch.qos.logback.contrib:logback-jackson is dual licensed as LGPL 2.1 or Eclipse Public License v1.0
# see https://github.com/qos-ch/logback-contrib/blob/master/license-template.txt
# cyclonedx identifies GNU Lesser General Public License
ignore {
  input.PkgName == "ch.qos.logback.contrib:logback-jackson"
  input.Name == "GNU Lesser General Public License"
}

# ch.qos.logback.contrib:logback-json-classic is dual licensed as LGPL 2.1 or Eclipse Public License v1.0
# see https://github.com/qos-ch/logback-contrib/blob/master/license-template.txt
# cyclonedx identifies GNU Lesser General Public License
ignore {
  input.PkgName == "ch.qos.logback.contrib:logback-json-classic"
  input.Name == "GNU Lesser General Public License"
}

# ch.qos.logback.contrib:logback-json-core is dual licensed as LGPL 2.1 or Eclipse Public License v1.0
# see https://github.com/qos-ch/logback-contrib/blob/master/license-template.txt
# cyclonedx identifies GNU Lesser General Public License
ignore {
  input.PkgName == "ch.qos.logback.contrib:logback-json-core"
  input.Name == "GNU Lesser General Public License"
}

# antlr-runtime is BSD-3-Clause (see https://www.antlr.org/license.html), cyclonedx identifies "BSD licence"
ignore {
  input.PkgName == "org.antlr:antlr-runtime"
  input.Name == "BSD licence"
}

# bnd writes Apache-2.0 OR EPL-2.0, both are open source licenses https://github.com/bndtools/bnd/blob/master/LICENSE
ignore {
  input.PkgName == "biz.aQute.bnd:biz.aQute.bnd.annotation"
  input.Name == "(Apache-2.0 OR EPL-2.0)"
}

# uses Go Lincense which is basically a BSD 3-Clause “New” or “Revised” License https://github.com/golang/go/blob/master/LICENSE & https://github.com/google/re2j/blob/master/LICENSE
ignore {
  input.PkgName == "com.google.re2j:re2j"
  input.Name == "Go License"
}