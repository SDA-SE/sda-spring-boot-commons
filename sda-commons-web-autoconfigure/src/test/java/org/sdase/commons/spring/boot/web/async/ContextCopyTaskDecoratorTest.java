/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.async;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.metadata.context.DetachedMetadataContext;
import org.sdase.commons.spring.boot.metadata.context.MetadataContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@SpringBootTest
@SpringBootApplication
class ContextCopyTaskDecoratorTest {

  @Autowired AsyncProcessor asyncProcessor;

  @BeforeEach
  @AfterEach
  void cleanup() {
    MetadataContext.createContext(new DetachedMetadataContext());
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  void shouldRunInNewThreadAndKeepMetadataContext() throws Exception {
    DetachedMetadataContext context = new DetachedMetadataContext();
    context.put("tenant-id", List.of("t-1"));
    MetadataContext.createContext(context);

    var actual = callAsyncAndVerify(asyncProcessor::process);

    assertThat(actual.get("metadata-context"))
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .containsEntry("tenant-id", List.of("t-1"));
  }

  @Test
  void shouldRunInNewThreadAndKeepRequestContext() throws Exception {
    var requestAttributes = mock(RequestAttributes.class);
    RequestContextHolder.setRequestAttributes(requestAttributes);

    var actual = callAsyncAndVerify(asyncProcessor::process);

    assertThat(actual.get("request-attributes")).isSameAs(requestAttributes);
  }

  @Test
  void shouldRunInNewThreadWithoutAnyContext() throws Exception {

    var actual = callAsyncAndVerify(asyncProcessor::process);

    assertThat(actual.get("metadata-context"))
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .isEmpty();
    assertThat(actual).doesNotContainKey("request-attributes");
  }

  Map<String, Object> callAsyncAndVerify(Callable<Future<Map<String, Object>>> callable)
      throws Exception {
    long threadId = Thread.currentThread().getId();
    var resultHolder = callable.call();
    assertThat(resultHolder).isNotDone();
    var result = resultHolder.get();
    assertThat(result.get("thread-id"))
        .isInstanceOf(Long.class)
        .isNotEqualTo(threadId)
        .isNotEqualTo(0L);
    return result;
  }

  @Component
  public static class AsyncProcessor {

    @Async
    public Future<Map<String, Object>> process() {
      await().pollDelay(1, TimeUnit.SECONDS).until(() -> true);
      var threadId = Thread.currentThread().getId();
      var metadataContext = MetadataContext.detachedCurrent();
      try {
        return new AsyncResult<>(
            Map.of(
                "thread-id",
                threadId,
                "metadata-context",
                metadataContext,
                "request-attributes",
                RequestContextHolder.currentRequestAttributes()));
      } catch (IllegalStateException ignored) {
        return new AsyncResult<>(
            Map.of("thread-id", threadId, "metadata-context", metadataContext));
      }
    }
  }
}
