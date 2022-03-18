/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.auth.opa.extension;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import org.sdase.commons.spring.boot.starter.auth.opa.model.OpaInput;

/**
 * An extension to provide additional data to the {@link OpaInput} before sending it to the Open
 * Policy Agent.
 *
 * <p>Implementing this class should be the <i>last resort</i>. It is more favourable to depend on
 * the existing {@link org.sdase.commons.spring.auth.opa.extension extensions} and instead use the
 * constraint feature to receive data from the policy decider and use it to decide based on it in
 * your service.
 *
 * <p>Each extension is added in an own namespace, that means that it's data is accessible in a
 * single sub-property. This contents can be represented by any object that is serializable by an
 * {@link ObjectMapper}.
 *
 * <p>The namespace is derived from the {@link Class#getSimpleName()} where the first letter is
 * converted to lowercase and {@code OpaInputExtension}, {@code InputExtension} or {@code Extension}
 * are removed from the end. The namespace can be customized by overwriting {@link #getNamespace()}.
 *
 * <p>Implementations need to be registered as {@link org.springframework.stereotype.Component}.
 * Then they are automatically discovered.
 *
 * <p>Extensions used in a single service should be documented for operators that need to set up
 * matching policies in the deployment.
 *
 * <p>Example that returns a boolean:
 *
 * <pre>
 *   {
 *     "jwt": "…",
 *     "path": ["…", "…"],
 *     "httpMethod": "GET",
 *     "myExtension": true
 *   }
 * </pre>
 *
 * <p>Example that returns an object:
 *
 * <pre>
 *   {
 *     "jwt": "…",
 *     "path": ["…", "…"],
 *     "httpMethod": "GET",
 *     "myExtension": {
 *       "myBoolean": true,
 *       "myString": "asdf",
 *       "myArray": ["…", "…", "…"]
 *     }
 *   }
 * </pre>
 */
public interface OpaInputExtension<T> {

  /**
   * @return the namespace of this extension that is used as property name holding the result of
   *     {@linkplain #createAdditionalInputContent(HttpServletRequest) the additional input content}
   */
  default String getNamespace() {
    var simpleName = getClass().getSimpleName();
    var normalizedName = simpleName.replaceAll("((Opa)?Input)?Extension$", "");
    return normalizedName.substring(0, 1).toLowerCase() + normalizedName.substring(1);
  }

  /**
   * @param request the current request
   * @return the JsonNode that should be added as child of the extension's {@link #getNamespace()}
   *     property.
   */
  T createAdditionalInputContent(HttpServletRequest request);
}
