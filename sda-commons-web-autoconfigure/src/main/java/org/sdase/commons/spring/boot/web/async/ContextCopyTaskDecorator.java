/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.async;

import org.sdase.commons.spring.boot.metadata.context.DetachedMetadataContext;
import org.sdase.commons.spring.boot.metadata.context.MetadataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * The TaskDecorator is annotated with {@code @Component} because it's then auto-applied by
 * {@linkplain org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration}
 */
@Component
public class ContextCopyTaskDecorator implements TaskDecorator {
  private static final Logger LOG = LoggerFactory.getLogger(ContextCopyTaskDecorator.class);

  @Override
  public Runnable decorate(Runnable runnable) {
    try {
      return new ContextAwareRunnable(
          runnable,
          RequestContextHolder.currentRequestAttributes(),
          MetadataContext.detachedCurrent());
    } catch (IllegalStateException e) {
      LOG.debug("Not transferring request attributes. Not in a request context?");
      return runnable;
    }
  }

  private record ContextAwareRunnable(
      Runnable task,
      RequestAttributes requestAttributes,
      DetachedMetadataContext detachedMetadataContext)
      implements Runnable {

    @Override
    public void run() {
      RequestContextHolder.setRequestAttributes(requestAttributes);
      MetadataContext.createContext(detachedMetadataContext);
      try {
        this.task.run();
      } finally {
        RequestContextHolder.resetRequestAttributes();
        MetadataContext.createContext(new DetachedMetadataContext());
      }
    }
  }
}
