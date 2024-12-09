/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.app.constraints;

// ATTENTION: The source of this class is included in the public documentation.

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.app.constraints.test.SomeConstraints;
import org.sdase.commons.spring.boot.web.app.constraints.test.SomeController;
import org.sdase.commons.spring.boot.web.app.constraints.test.SomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = SomeController.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class SomeControllerTest {

  @MockitoBean SomeConstraints mockConstraints;
  @MockitoBean SomeService someService;

  @Autowired SomeController constraintsAwareController;

  @Test
  void shouldBeAdmin() {
    when(mockConstraints.isAdmin()).thenReturn(true);
    constraintsAwareController.getUserCategory();
    verify(someService, times(1)).doAsAdmin();
    verifyNoMoreInteractions(someService);
  }

  @Test
  void shouldBeUser() {
    when(mockConstraints.isAdmin()).thenReturn(false);
    constraintsAwareController.getUserCategory();
    verify(someService, times(1)).doAsUser();
    verifyNoMoreInteractions(someService);
  }
}
