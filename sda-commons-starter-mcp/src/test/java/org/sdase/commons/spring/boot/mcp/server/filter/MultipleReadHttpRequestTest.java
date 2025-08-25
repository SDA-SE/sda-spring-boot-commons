/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.server.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class MultipleReadHttpRequestTest {

  @Test
  void shouldCacheRequestBodyAndAllowMultipleReads() throws IOException {
    String requestBody = "{ \"message\": \"Hello World\" }";
    byte[] requestBytes = requestBody.getBytes(StandardCharsets.UTF_8);

    HttpServletRequest originalRequest = mock(HttpServletRequest.class);
    ServletInputStream inputStream = createServletInputStream(requestBytes);
    when(originalRequest.getInputStream()).thenReturn(inputStream);

    MultipleReadHttpRequest multipleReadRequest = new MultipleReadHttpRequest(originalRequest);

    ServletInputStream firstRead = multipleReadRequest.getInputStream();
    byte[] firstReadBytes = firstRead.readAllBytes();
    assertThat(new String(firstReadBytes, StandardCharsets.UTF_8)).isEqualTo(requestBody);

    ServletInputStream secondRead = multipleReadRequest.getInputStream();
    byte[] secondReadBytes = secondRead.readAllBytes();
    assertThat(new String(secondReadBytes, StandardCharsets.UTF_8)).isEqualTo(requestBody);
  }

  @Test
  void shouldProvideBufferedReaderForMultipleReads() throws IOException {
    String requestBody = "Line 1\nLine 2\nLine 3";
    byte[] requestBytes = requestBody.getBytes(StandardCharsets.UTF_8);

    HttpServletRequest originalRequest = mock(HttpServletRequest.class);
    ServletInputStream inputStream = createServletInputStream(requestBytes);
    when(originalRequest.getInputStream()).thenReturn(inputStream);

    MultipleReadHttpRequest multipleReadRequest = new MultipleReadHttpRequest(originalRequest);

    BufferedReader firstReader = multipleReadRequest.getReader();
    String firstReadContent =
        firstReader.lines().reduce("", (a, b) -> a.isEmpty() ? b : a + "\n" + b);
    assertThat(firstReadContent).isEqualTo(requestBody);

    BufferedReader secondReader = multipleReadRequest.getReader();
    String secondReadContent =
        secondReader.lines().reduce("", (a, b) -> a.isEmpty() ? b : a + "\n" + b);
    assertThat(secondReadContent).isEqualTo(requestBody);
  }

  @Test
  void shouldMixInputStreamAndBufferedReaderReads() throws IOException {
    String requestBody = "Mixed read test content";
    byte[] requestBytes = requestBody.getBytes(StandardCharsets.UTF_8);

    HttpServletRequest originalRequest = mock(HttpServletRequest.class);
    ServletInputStream inputStream = createServletInputStream(requestBytes);
    when(originalRequest.getInputStream()).thenReturn(inputStream);

    MultipleReadHttpRequest multipleReadRequest = new MultipleReadHttpRequest(originalRequest);

    ServletInputStream firstRead = multipleReadRequest.getInputStream();
    byte[] firstReadBytes = firstRead.readAllBytes();
    assertThat(new String(firstReadBytes, StandardCharsets.UTF_8)).isEqualTo(requestBody);

    BufferedReader reader = multipleReadRequest.getReader();
    String readerContent = reader.lines().reduce("", (a, b) -> a.isEmpty() ? b : a + "\n" + b);
    assertThat(readerContent).isEqualTo(requestBody);

    ServletInputStream secondRead = multipleReadRequest.getInputStream();
    byte[] secondReadBytes = secondRead.readAllBytes();
    assertThat(new String(secondReadBytes, StandardCharsets.UTF_8)).isEqualTo(requestBody);
  }

  @Test
  void shouldHandleEmptyRequestBody() throws IOException {
    HttpServletRequest originalRequest = mock(HttpServletRequest.class);
    ServletInputStream emptyInputStream = createServletInputStream(new byte[0]);
    when(originalRequest.getInputStream()).thenReturn(emptyInputStream);

    MultipleReadHttpRequest multipleReadRequest = new MultipleReadHttpRequest(originalRequest);

    ServletInputStream inputStream = multipleReadRequest.getInputStream();
    byte[] content = inputStream.readAllBytes();
    assertThat(content).isEmpty();

    BufferedReader reader = multipleReadRequest.getReader();
    String readerContent = reader.lines().reduce("", (a, b) -> a.isEmpty() ? b : a + "\n" + b);
    assertThat(readerContent).isEmpty();
  }

  @Test
  void shouldHandleLargeRequestBody() throws IOException {
    StringBuilder largeContent = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      largeContent.append("This is line ").append(i).append(" of a large request body.\n");
    }
    String requestBody = largeContent.toString();
    byte[] requestBytes = requestBody.getBytes(StandardCharsets.UTF_8);

    HttpServletRequest originalRequest = mock(HttpServletRequest.class);
    ServletInputStream inputStream = createServletInputStream(requestBytes);
    when(originalRequest.getInputStream()).thenReturn(inputStream);

    MultipleReadHttpRequest multipleReadRequest = new MultipleReadHttpRequest(originalRequest);

    ServletInputStream firstRead = multipleReadRequest.getInputStream();
    byte[] firstReadBytes = firstRead.readAllBytes();
    assertThat(new String(firstReadBytes, StandardCharsets.UTF_8)).isEqualTo(requestBody);

    ServletInputStream secondRead = multipleReadRequest.getInputStream();
    byte[] secondReadBytes = secondRead.readAllBytes();
    assertThat(new String(secondReadBytes, StandardCharsets.UTF_8)).isEqualTo(requestBody);
  }

  @Test
  void shouldPropagateIOExceptionFromOriginalRequest() throws IOException {
    HttpServletRequest originalRequest = mock(HttpServletRequest.class);
    when(originalRequest.getInputStream()).thenThrow(new IOException("Test IO exception"));

    assertThatThrownBy(() -> new MultipleReadHttpRequest(originalRequest))
        .isInstanceOf(IOException.class)
        .hasMessage("Test IO exception");
  }

  @Test
  void shouldReturnCachedBodyServletInputStream() throws IOException {
    String requestBody = "Test content";
    byte[] requestBytes = requestBody.getBytes(StandardCharsets.UTF_8);

    HttpServletRequest originalRequest = mock(HttpServletRequest.class);
    ServletInputStream inputStream = createServletInputStream(requestBytes);
    when(originalRequest.getInputStream()).thenReturn(inputStream);

    MultipleReadHttpRequest multipleReadRequest = new MultipleReadHttpRequest(originalRequest);
    ServletInputStream cachedInputStream = multipleReadRequest.getInputStream();

    assertThat(cachedInputStream).isInstanceOf(CachedBodyServletInputStream.class);
    assertThat(cachedInputStream.isReady()).isTrue();
    assertThat(cachedInputStream.isFinished()).isFalse();

    cachedInputStream.readAllBytes();
    assertThat(cachedInputStream.isFinished()).isTrue();
  }

  private ServletInputStream createServletInputStream(byte[] data) {
    return new ServletInputStream() {
      private final ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

      @Override
      public boolean isFinished() {
        return inputStream.available() == 0;
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setReadListener(jakarta.servlet.ReadListener readListener) {
        // Not implemented for test
      }

      @Override
      public int read() {
        return inputStream.read();
      }
    };
  }
}
