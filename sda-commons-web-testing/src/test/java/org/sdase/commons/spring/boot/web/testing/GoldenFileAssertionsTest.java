/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GoldenFileAssertionsTest {

  @TempDir private Path tempDir;

  private Path tempFile;

  private CiUtil ciUtilMockTrue = mock(CiUtil.class);
  private CiUtil ciUtilMockFalse = mock(CiUtil.class);

  @BeforeEach
  void setUp() {

    when(ciUtilMockTrue.isRunningInCiPipeline()).thenReturn(true);
    when(ciUtilMockFalse.isRunningInCiPipeline()).thenReturn(false);
    tempFile = tempDir.resolve("file");
  }

  @Test
  void textShouldNotThrowOnCorrectFileContent() throws IOException {
    // create file with expected-content
    Files.write(tempFile, "expected-content".getBytes());

    // should be accepted
    assertThatCode(
            () ->
                GoldenFileAssertions.assertThat(tempFile)
                    .withCiUtil(ciUtilMockFalse)
                    .hasContentAndUpdateGolden("expected-content"))
        .doesNotThrowAnyException();

    // content should still be expected-content
    assertThat(tempFile).hasContent("expected-content");
  }

  @Test
  void textShouldNotThrowOnCorrectFileContentWithSpecialCharacters() throws IOException {
    // create file with expected-content
    Files.writeString(tempFile, "expected-content-\u00f6");

    // should be accepted
    assertThatCode(
            () ->
                GoldenFileAssertions.assertThat(tempFile)
                    .withCiUtil(ciUtilMockFalse)
                    .hasContentAndUpdateGolden("expected-content-ö"))
        .doesNotThrowAnyException();

    // content should still be expected-content
    assertThat(tempFile).hasBinaryContent("expected-content-ö".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void textShouldThrowOnInvalidFileContent() throws IOException {
    // create file with unexpected-content
    Files.write(tempFile, "unexpected-content".getBytes());

    // should throw and update the file
    GoldenFileAssertions goldenFileAssertions =
        GoldenFileAssertions.assertThat(tempFile).withCiUtil(ciUtilMockFalse);
    assertThatThrownBy(() -> goldenFileAssertions.hasContentAndUpdateGolden("expected-content"))
        .isInstanceOf(AssertionError.class)
        .hasMessageContaining(
            "The current %s file is not up-to-date. If this happens locally,",
            tempFile.getFileName().toString());

    // content should now be expected-content
    assertThat(tempFile).hasContent("expected-content");
  }

  @Test
  void textShouldThrowOnMissingFile() {
    // use a file that does not yet exist
    Path path = tempDir.resolve("non-existing-file.yaml");

    // should throw and update the file
    GoldenFileAssertions goldenFileAssertions =
        GoldenFileAssertions.assertThat(path).withCiUtil(ciUtilMockFalse);
    assertThatThrownBy(() -> goldenFileAssertions.hasContentAndUpdateGolden("expected-content"))
        .isInstanceOf(AssertionError.class)
        .hasMessageContaining(
            "The current %s file is not up-to-date. If this happens locally,",
            path.getFileName().toString());

    // content should now be expected-content
    assertThat(path).exists().hasContent("expected-content");
  }

  @Test
  void yamlShouldNotThrowOnCorrectFileContent() throws IOException {
    // create file with expected-content
    Files.write(tempFile, "key0: v\nkey1: w\nkey2:\n  nested1: a\n  nested2: b".getBytes());

    // should be accepted
    assertThatCode(
            () ->
                GoldenFileAssertions.assertThat(tempFile)
                    .withCiUtil(ciUtilMockFalse)
                    .hasYamlContentAndUpdateGolden(
                        "key0: v\nkey2:\n  nested2: b\n  nested1: a\nkey1: w"))
        .doesNotThrowAnyException();

    // content should still be expected-content
    assertThat(tempFile).hasContent("key0: v\nkey2:\n  nested2: b\n  nested1: a\nkey1: w");
  }

  @Test
  void yamlShouldNotThrowOnCorrectFileContentWithSpecialCharacters() throws IOException {
    // create file with expected-content
    Files.writeString(tempFile, "key0: v\nkey1: w\nkey2:\n  nested1: a\n  nested2: \u00f6");

    // should be accepted
    assertThatCode(
            () ->
                GoldenFileAssertions.assertThat(tempFile)
                    .withCiUtil(ciUtilMockFalse)
                    .hasYamlContentAndUpdateGolden(
                        "key0: v\nkey2:\n  nested2: ö\n  nested1: a\nkey1: w"))
        .doesNotThrowAnyException();

    // content should still be expected-content
    assertThat(tempFile)
        .hasBinaryContent(
            "key0: v\nkey2:\n  nested2: \u00f6\n  nested1: a\nkey1: w"
                .getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void yamlShouldThrowOnInvalidFileContent() throws IOException {
    // create file with unexpected-content
    Files.write(tempFile, "key0: v\nkey1: w\nkey2:\n  nested1: a\n  nested2: b".getBytes());

    // should throw and update the file
    GoldenFileAssertions goldenFileAssertions =
        GoldenFileAssertions.assertThat(tempFile).withCiUtil(ciUtilMockFalse);
    assertThatThrownBy(
            () ->
                goldenFileAssertions.hasYamlContentAndUpdateGolden(
                    "key0: w\nkey1: x\nkey2:\n  nested1: b\n  nested2: c"))
        .isInstanceOf(AssertionError.class)
        .hasMessageContaining(
            "The current %s file is not up-to-date. If this happens locally,",
            tempFile.getFileName().toString());

    // content should now be expected-content
    assertThat(tempFile).hasContent("key0: w\nkey1: x\nkey2:\n  nested1: b\n  nested2: c");
  }

  @Test
  void jsonShouldNotThrowOnCorrectFileContent() throws IOException {
    // create file with expected-content
    Files.write(
        tempFile,
        "{\"key0\": \"v\",\"key1\": \"w\",\"key2\":{\"nested1\":\"a\",\"nested2\": \"b\"}}"
            .getBytes());

    // should be accepted
    assertThatCode(
            () ->
                GoldenFileAssertions.assertThat(tempFile)
                    .withCiUtil(ciUtilMockFalse)
                    .hasYamlContentAndUpdateGolden(
                        "{\"key0\": \"v\",\"key2\":{\"nested2\":\"b\",\"nested1\": \"a\"},\"key1\": \"w\"}"))
        .doesNotThrowAnyException();

    // content should still be expected-content
    assertThat(tempFile)
        .hasContent(
            "{\"key0\": \"v\",\"key2\":{\"nested2\":\"b\",\"nested1\": \"a\"},\"key1\": \"w\"}");
  }

  @Test
  void jsonShouldThrowOnInvalidFileContent() throws IOException {
    // create file with unexpected-content
    Files.write(
        tempFile,
        ("{\"key0\": \"v\",\"key1\": \"w\",\"key2\":{\"nested1\":\"a\",\"nested2\": \"b\"}}")
            .getBytes());

    // should throw and update the file
    GoldenFileAssertions goldenFileAssertions =
        GoldenFileAssertions.assertThat(tempFile).withCiUtil(ciUtilMockFalse);
    assertThatThrownBy(
            () ->
                goldenFileAssertions.hasYamlContentAndUpdateGolden(
                    "{\"key0\": \"2\",\"key1\": \"x\",\"key2\":{\"nested1\":\"b\",\"nested2\": \"c\"}}"))
        .isInstanceOf(AssertionError.class)
        .hasMessageContaining(
            "The current %s file is not up-to-date. If this happens locally,",
            tempFile.getFileName().toString());

    // content should now be expected-content
    assertThat(tempFile)
        .hasContent(
            "{\"key0\": \"2\",\"key1\": \"x\",\"key2\":{\"nested1\":\"b\",\"nested2\": \"c\"}}");
  }

  @Test
  void yamlShouldThrowOnMissingFile() {
    // use a file that does not yet exist
    Path path = tempDir.resolve("non-existing-file.yaml");

    // should throw and update the file
    GoldenFileAssertions goldenFileAssertions =
        GoldenFileAssertions.assertThat(path).withCiUtil(ciUtilMockFalse);
    assertThatThrownBy(() -> goldenFileAssertions.hasYamlContentAndUpdateGolden("expected-content"))
        .isInstanceOf(AssertionError.class)
        .hasMessageContaining(
            "The current %s file is not up-to-date. If this happens locally,",
            path.getFileName().toString());

    // content should now be expected-content
    assertThat(path).exists().hasContent("expected-content");
  }

  @Test
  void hasYamlContentAndUpdateGoldenShouldNeverUpdateContentInCi() throws IOException {
    var path = tempDir.resolve("file.yaml");
    var oldContent = "foo";
    Files.write(path, oldContent.getBytes());

    var goldenFileAssertions = GoldenFileAssertions.assertThat(path).withCiUtil(ciUtilMockTrue);
    var newContent = "bar";
    assertThatThrownBy(() -> goldenFileAssertions.hasYamlContentAndUpdateGolden(newContent))
        .isInstanceOf(AssertionError.class);

    // content should never change
    assertThat(path).hasContent(oldContent);
  }

  @Test
  void hasContentAndUpdateGoldenShouldNeverUpdateContentInCi() throws IOException {
    var path = tempDir.resolve("file.yaml");
    var oldContent = "foo";
    Files.write(path, oldContent.getBytes());

    var goldenFileAssertions = GoldenFileAssertions.assertThat(path).withCiUtil(ciUtilMockTrue);
    var newContent = "bar";
    assertThatThrownBy(() -> goldenFileAssertions.hasContentAndUpdateGolden(newContent))
        .isInstanceOf(AssertionError.class);

    // content should never change
    assertThat(path).hasContent(oldContent);
  }

  @Test
  void hasYamlContentAndUpdateGoldenShouldAlwaysUpdateContent() throws IOException {
    var path = tempDir.resolve("file.yaml");
    var oldContent = "foo";
    Files.write(path, oldContent.getBytes());

    var goldenFileAssertions = GoldenFileAssertions.assertThat(path).withCiUtil(ciUtilMockFalse);
    var newContent = "bar";
    assertThatThrownBy(() -> goldenFileAssertions.hasYamlContentAndUpdateGolden(newContent))
        .isInstanceOf(AssertionError.class);

    // content should never change
    assertThat(path).hasContent(newContent);
  }

  @Test
  void hasContentAndUpdateGoldenShouldAlwaysUpdateContent() throws IOException {
    var path = tempDir.resolve("file.yaml");
    var oldContent = "foo";
    Files.write(path, oldContent.getBytes());

    var goldenFileAssertions = GoldenFileAssertions.assertThat(path).withCiUtil(ciUtilMockFalse);
    var newContent = "bar";
    assertThatThrownBy(() -> goldenFileAssertions.hasContentAndUpdateGolden(newContent))
        .isInstanceOf(AssertionError.class);

    // content should never change
    assertThat(path).hasContent(newContent);
  }
}
