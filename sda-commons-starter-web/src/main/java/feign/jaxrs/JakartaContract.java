/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package feign.jaxrs;

import feign.jaxrs3.JAXRS3Contract;

/**
 * Due to some <a
 * href="https://github.com/OpenFeign/feign/commit/ccb706a8f940cedd0f7e54139724b7177a13785d#diff-3bcf0ff3c5551de41e71ddd75db4d6f71ad4c7935a665abfc537a66c0bbd2714R37-R42">Maven
 * Magic</a> that prevents loading the dependency with gradle, we added this deprecated class
 * temporarily here.
 *
 * @deprecated use {@link JAXRS3Contract} instead, {@code JakartaContract} will be removed in the
 *     next major of SDA Spring Boot Commons!
 */
@Deprecated(forRemoval = true)
@SuppressWarnings({"java:S110", "java:S1133", "DeprecatedIsStillUsed"})
public class JakartaContract extends JAXRS3Contract {}
