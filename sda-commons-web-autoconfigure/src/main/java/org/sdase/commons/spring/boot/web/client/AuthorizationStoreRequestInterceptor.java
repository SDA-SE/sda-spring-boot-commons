/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.web.client;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class AuthorizationStoreRequestInterceptor implements HandlerInterceptor {

  public static final String ATTRIBUTE_NAME =
      AuthorizationStoreRequestInterceptor.class.getName() + ".authHeaderValue";
  public static final int SCOPE = RequestAttributes.SCOPE_REQUEST;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    var authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorizationHeader != null) {
      request.setAttribute(ATTRIBUTE_NAME, authorizationHeader);
    }

    return HandlerInterceptor.super.preHandle(request, response, handler);
  }

  @Override
  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      ModelAndView modelAndView)
      throws Exception {
    request.removeAttribute(ATTRIBUTE_NAME);
    HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
  }
}
