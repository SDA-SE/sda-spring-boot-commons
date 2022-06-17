/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.web.auth.opa;

import static org.sdase.commons.spring.boot.web.auth.opa.OpaAccessDecisionVoter.CONSTRAINTS_ATTRIBUTE;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.annotation.PostConstruct;
import org.sdase.commons.spring.boot.web.auth.opa.model.OpaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * A helper that provides constraints received from the Open Policy Agent as a bean. This class
 * should be used as a super class of a specific constraints model. To provide a bean that can be
 * {@link Autowired} with the desired request scope, the constraint model must be annotated with
 * {@link Constraints}. The constraints model class should not declare a constructor. The default
 * values must ensure restrictive constraints.
 *
 * <p>Example:
 *
 * <pre>
 *   <code>{@literal @}{@link Constraints}
 *     public class MyCustomConstraints extend {@link AbstractConstraints} {
 *       private List&lt;String&gt; allowedIds = new ArrayList&lt;&gt;();
 *       private boolean admin;
 *       // getters and setters omitted
 *     }
 *   </code>
 * </pre>
 *
 * {@code MyCustomConstraints} can be autowired in a {@link
 * org.springframework.web.bind.annotation.RestController}.
 */
public abstract class AbstractConstraints {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractConstraints.class);

  // use unrecommended field injection to avoid influence on implementation classes so that they can
  // have a no args constructor
  @Autowired private ObjectMapper objectMapper;

  @PostConstruct
  void process() {
    try {
      var requestAttributes = RequestContextHolder.currentRequestAttributes();
      var opaResponse =
          (OpaResponse)
              requestAttributes.getAttribute(
                  CONSTRAINTS_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
      if (opaResponse != null) {
        objectMapper.updateValue(this, opaResponse.getResult());
      }
    } catch (IllegalStateException e) {
      LOG.debug("Not in a request context.");
    } catch (ClassCastException | NullPointerException | JsonMappingException e) {
      LOG.warn("Failed to apply constraints of type {}", this.getClass(), e);
    }
  }
}
