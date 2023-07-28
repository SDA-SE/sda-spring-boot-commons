/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth.opa.extension;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OpaInputExtensionTest {

  @ParameterizedTest
  @ValueSource(
      classes = {
        CustomDataOpaInputExtension.class,
        CustomDataInputExtension.class,
        CustomDataExtension.class,
        CustomData.class,
        FancyName.class
      })
  void shouldNormalizeNamespaceFromClassName(Class<? extends OpaInputExtension<?>> given)
      throws NoSuchMethodException,
          InvocationTargetException,
          InstantiationException,
          IllegalAccessException {
    var opaInputExtension = given.getDeclaredConstructor().newInstance();
    assertThat(opaInputExtension.getNamespace()).isEqualTo("customData");
  }

  static class CustomDataOpaInputExtension implements OpaInputExtension<Object> {
    @Override
    public Object createAdditionalInputContent(HttpServletRequest request) {
      return null;
    }
  }

  static class CustomDataInputExtension implements OpaInputExtension<Object> {
    @Override
    public Object createAdditionalInputContent(HttpServletRequest request) {
      return null;
    }
  }

  static class CustomDataExtension implements OpaInputExtension<Object> {
    @Override
    public Object createAdditionalInputContent(HttpServletRequest request) {
      return null;
    }
  }

  static class CustomData implements OpaInputExtension<Object> {
    @Override
    public Object createAdditionalInputContent(HttpServletRequest request) {
      return null;
    }
  }

  static class FancyName implements OpaInputExtension<Object> {
    @Override
    public String getNamespace() {
      return "customData";
    }

    @Override
    public Object createAdditionalInputContent(HttpServletRequest request) {
      return null;
    }
  }
}
