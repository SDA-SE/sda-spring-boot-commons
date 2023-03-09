/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.metadata.context;

/**
 * Strategies for merging data from a {@link DetachedMetadataContext} into the {@linkplain
 * MetadataContext#current() current metadata context}.
 */
public enum MetadataContextMergeStrategy {

  /** All values for a specific key will be merged into a distinct list of values. */
  EXTEND,

  /**
   * New values take precedence. If new values for a specific key exist, already existing values
   * will be replaced by the new values. If no new values exist, old values are kept.
   */
  REPLACE,

  /**
   * Existing values take precedence. If values for a specific key exist in the current context,
   * they are kept and new values for this key are ignored. If no values for a specific key exist in
   * the current context, the new values are used.
   */
  KEEP
}
