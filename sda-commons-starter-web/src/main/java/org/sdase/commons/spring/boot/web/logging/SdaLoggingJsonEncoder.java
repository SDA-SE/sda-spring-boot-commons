/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.logging;

import static ch.qos.logback.core.CoreConstants.COMMA_CHAR;
import static ch.qos.logback.core.CoreConstants.DOUBLE_QUOTE_CHAR;
import static ch.qos.logback.core.CoreConstants.UTF_8_CHARSET;
import static ch.qos.logback.core.encoder.JsonEscapeUtil.jsonEscapeString;
import static ch.qos.logback.core.model.ModelConstants.NULL_STR;

import ch.qos.logback.classic.encoder.JsonEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.CoreConstants;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;

public class SdaLoggingJsonEncoder extends JsonEncoder {

  static int DEFAULT_SIZE = 1024;
  static int DEFAULT_SIZE_WITH_THROWABLE = DEFAULT_SIZE * 8;

  public static final String CONTEXT_ATTR_NAME = "context";
  public static final String NAME_ATTR_NAME = "name";
  public static final String BIRTHDATE_ATTR_NAME = "birthdate";
  public static final String CONTEXT_PROPERTIES_ATTR_NAME = "properties";

  public static final String TIMESTAMP_ATTR_NAME = "timestamp";

  public static final String LEVEL_ATTR_NAME = "level";
  public static final String MARKERS_ATTR_NAME = "markers";
  public static final String THREAD_NAME_ATTR_NAME = "thread";
  public static final String MDC_ATTR_NAME = "mdc";
  public static final String LOGGER_ATTR_NAME = "logger";

  public static final String MESSAGE_ATTR_NAME = "message";

  public static final String ARGUMENT_ARRAY_ATTR_NAME = "arguments";
  public static final String KEY_VALUE_PAIRS_ATTR_NAME = "kvpList";

  public static final String THROWABLE_ATTR_NAME = "throwable";

  private static final String CYCLIC_THROWABLE_ATTR_NAME = "cyclic";

  public static final String CAUSE_ATTR_NAME = "cause";

  public static final String SUPPRESSED_ATTR_NAME = "suppressed";

  public static final String COMMON_FRAMES_COUNT_ATTR_NAME = "commonFramesCount";

  public static final String CLASS_NAME_ATTR_NAME = "className";
  public static final String METHOD_NAME_ATTR_NAME = "methodName";
  private static final String FILE_NAME_ATTR_NAME = "fileName";
  private static final String LINE_NUMBER_ATTR_NAME = "lineNumber";

  public static final String STEP_ARRAY_NAME_ATTRIBUTE = "stepArray";

  private static final char OPEN_OBJ = '{';
  private static final char CLOSE_OBJ = '}';
  private static final char OPEN_ARRAY = '[';
  private static final char CLOSE_ARRAY = ']';

  private static final char QUOTE = DOUBLE_QUOTE_CHAR;
  private static final char SP = ' ';

  private static final String QUOTE_COL = "\":";

  private static final char VALUE_SEPARATOR = COMMA_CHAR;

  @Override
  public byte[] encode(ILoggingEvent event) {
    final int initialCapacity =
        event.getThrowableProxy() == null ? DEFAULT_SIZE : DEFAULT_SIZE_WITH_THROWABLE;
    StringBuilder sb = new StringBuilder(initialCapacity);
    sb.append(OPEN_OBJ);

    appenderMemberWithLongValue(sb, TIMESTAMP_ATTR_NAME, event.getTimeStamp());
    sb.append(VALUE_SEPARATOR);

    String levelStr = event.getLevel() != null ? event.getLevel().levelStr : NULL_STR;
    appenderMember(sb, LEVEL_ATTR_NAME, levelStr);
    sb.append(VALUE_SEPARATOR);

    appenderMember(sb, THREAD_NAME_ATTR_NAME, jsonEscape(event.getThreadName()));
    sb.append(VALUE_SEPARATOR);

    appenderMember(sb, LOGGER_ATTR_NAME, event.getLoggerName());
    sb.append(VALUE_SEPARATOR);

    appendLoggerContext(sb, event.getLoggerContextVO());
    sb.append(VALUE_SEPARATOR);

    appendMarkers(sb, event);

    appendMDC(sb, event);

    appendKeyValuePairs(sb, event);

    appenderMember(sb, MESSAGE_ATTR_NAME, jsonEscape(event.getMessage()));
    sb.append(VALUE_SEPARATOR);

    appendArgumentArray(sb, event);

    appendThrowableProxy(sb, THROWABLE_ATTR_NAME, event.getThrowableProxy());
    sb.append(CLOSE_OBJ);
    sb.append(CoreConstants.JSON_LINE_SEPARATOR);
    return sb.toString().getBytes(UTF_8_CHARSET);
  }

  private void appendLoggerContext(StringBuilder sb, LoggerContextVO loggerContextVO) {

    sb.append(QUOTE).append(CONTEXT_ATTR_NAME).append(QUOTE_COL);
    if (loggerContextVO == null) {
      sb.append(NULL_STR);
      return;
    }

    sb.append(OPEN_OBJ);
    appenderMember(sb, NAME_ATTR_NAME, nullSafeStr(loggerContextVO.getName()));
    sb.append(VALUE_SEPARATOR);
    appenderMemberWithLongValue(sb, BIRTHDATE_ATTR_NAME, loggerContextVO.getBirthTime());
    sb.append(VALUE_SEPARATOR);

    appendMap(sb, CONTEXT_PROPERTIES_ATTR_NAME, loggerContextVO.getPropertyMap());
    sb.append(CLOSE_OBJ);
  }

  private void appendMap(StringBuilder sb, String attrName, Map<String, String> map) {
    sb.append(QUOTE).append(attrName).append(QUOTE_COL);
    if (map == null) {
      sb.append(NULL_STR);
      return;
    }

    sb.append(OPEN_OBJ);

    boolean addComma = false;
    Set<Entry<String, String>> entries = map.entrySet();
    for (Map.Entry<String, String> entry : entries) {
      if (addComma) {
        sb.append(VALUE_SEPARATOR);
      }
      addComma = true;
      appenderMember(
          sb, jsonEscapedToString(entry.getKey()), jsonEscapedToString(entry.getValue()));
    }

    sb.append(CLOSE_OBJ);
  }

  private void appendThrowableProxy(StringBuilder sb, String attributeName, IThrowableProxy itp) {

    // in the nominal case, attributeName != null. However, attributeName will be null for
    // suppressed
    // IThrowableProxy array, in which case no attribute name is needed
    if (attributeName != null) {
      sb.append(QUOTE).append(attributeName).append(QUOTE_COL);
      if (itp == null) {
        sb.append(NULL_STR);
        return;
      }
    }

    sb.append(OPEN_OBJ);

    appenderMember(sb, CLASS_NAME_ATTR_NAME, nullSafeStr(itp.getClassName()));

    sb.append(VALUE_SEPARATOR);
    appenderMember(sb, MESSAGE_ATTR_NAME, jsonEscape(itp.getMessage()));

    if (itp.isCyclic()) {
      sb.append(VALUE_SEPARATOR);
      appenderMember(sb, CYCLIC_THROWABLE_ATTR_NAME, jsonEscape("true"));
    }

    sb.append(VALUE_SEPARATOR);
    appendSTEPArray(sb, itp.getStackTraceElementProxyArray(), itp.getCommonFrames());

    if (itp.getCommonFrames() != 0) {
      sb.append(VALUE_SEPARATOR);
      appenderMemberWithIntValue(sb, COMMON_FRAMES_COUNT_ATTR_NAME, itp.getCommonFrames());
    }

    IThrowableProxy cause = itp.getCause();
    if (cause != null) {
      sb.append(VALUE_SEPARATOR);
      appendThrowableProxy(sb, CAUSE_ATTR_NAME, cause);
    }

    IThrowableProxy[] suppressedArray = itp.getSuppressed();
    if (suppressedArray != null && suppressedArray.length != 0) {
      sb.append(VALUE_SEPARATOR);
      sb.append(QUOTE).append(SUPPRESSED_ATTR_NAME).append(QUOTE_COL);
      sb.append(OPEN_ARRAY);
      boolean first = true;
      for (IThrowableProxy suppressedITP : suppressedArray) {
        if (first) {
          first = false;
        } else {
          sb.append(VALUE_SEPARATOR);
        }
        appendThrowableProxy(sb, null, suppressedITP);
      }
      sb.append(CLOSE_ARRAY);
    }

    sb.append(CLOSE_OBJ);
  }

  private void appendSTEPArray(
      StringBuilder sb, StackTraceElementProxy[] stepArray, int commonFrames) {
    sb.append(QUOTE).append(STEP_ARRAY_NAME_ATTRIBUTE).append(QUOTE_COL).append(OPEN_ARRAY);

    int len = stepArray != null ? stepArray.length : 0;

    if (commonFrames >= len) {
      commonFrames = 0;
    }

    for (int i = 0; i < len - commonFrames; i++) {
      if (i != 0) sb.append(VALUE_SEPARATOR);

      StackTraceElementProxy step = stepArray[i];

      sb.append(OPEN_OBJ);
      StackTraceElement ste = step.getStackTraceElement();

      appenderMember(sb, CLASS_NAME_ATTR_NAME, nullSafeStr(ste.getClassName()));
      sb.append(VALUE_SEPARATOR);

      appenderMember(sb, METHOD_NAME_ATTR_NAME, nullSafeStr(ste.getMethodName()));
      sb.append(VALUE_SEPARATOR);

      appenderMember(sb, FILE_NAME_ATTR_NAME, nullSafeStr(ste.getFileName()));
      sb.append(VALUE_SEPARATOR);

      appenderMemberWithIntValue(sb, LINE_NUMBER_ATTR_NAME, ste.getLineNumber());
      sb.append(CLOSE_OBJ);
    }

    sb.append(CLOSE_ARRAY);
  }

  private void appenderMember(StringBuilder sb, String key, String value) {
    sb.append(QUOTE).append(key).append(QUOTE_COL).append(QUOTE).append(value).append(QUOTE);
  }

  private void appenderMemberWithIntValue(StringBuilder sb, String key, int value) {
    sb.append(QUOTE).append(key).append(QUOTE_COL).append(value);
  }

  private void appenderMemberWithLongValue(StringBuilder sb, String key, long value) {
    sb.append(QUOTE).append(key).append(QUOTE_COL).append(value);
  }

  private void appendKeyValuePairs(StringBuilder sb, ILoggingEvent event) {
    List<KeyValuePair> kvpList = event.getKeyValuePairs();
    if (kvpList == null || kvpList.isEmpty()) return;

    sb.append(QUOTE)
        .append(KEY_VALUE_PAIRS_ATTR_NAME)
        .append(QUOTE_COL)
        .append(SP)
        .append(OPEN_ARRAY);
    final int len = kvpList.size();
    for (int i = 0; i < len; i++) {
      if (i != 0) sb.append(VALUE_SEPARATOR);
      KeyValuePair kvp = kvpList.get(i);
      sb.append(OPEN_OBJ);
      appenderMember(sb, jsonEscapedToString(kvp.key), jsonEscapedToString(kvp.value));
      sb.append(CLOSE_OBJ);
    }
    sb.append(CLOSE_ARRAY);
    sb.append(VALUE_SEPARATOR);
  }

  private void appendArgumentArray(StringBuilder sb, ILoggingEvent event) {
    Object[] argumentArray = event.getArgumentArray();
    if (argumentArray == null) return;

    sb.append(QUOTE)
        .append(ARGUMENT_ARRAY_ATTR_NAME)
        .append(QUOTE_COL)
        .append(SP)
        .append(OPEN_ARRAY);
    final int len = argumentArray.length;
    for (int i = 0; i < len; i++) {
      if (i != 0) sb.append(VALUE_SEPARATOR);
      sb.append(QUOTE).append(jsonEscapedToString(argumentArray[i])).append(QUOTE);
    }
    sb.append(CLOSE_ARRAY);
    sb.append(VALUE_SEPARATOR);
  }

  private void appendMarkers(StringBuilder sb, ILoggingEvent event) {
    List<Marker> markerList = event.getMarkerList();
    if (markerList == null) return;

    sb.append(QUOTE).append(MARKERS_ATTR_NAME).append(QUOTE_COL).append(SP).append(OPEN_ARRAY);
    final int len = markerList.size();
    for (int i = 0; i < len; i++) {
      if (i != 0) sb.append(VALUE_SEPARATOR);
      sb.append(QUOTE).append(jsonEscapedToString(markerList.get(i))).append(QUOTE);
    }
    sb.append(CLOSE_ARRAY);
    sb.append(VALUE_SEPARATOR);
  }

  private String jsonEscapedToString(Object o) {
    if (o == null) return NULL_STR;
    return jsonEscapeString(o.toString());
  }

  private String nullSafeStr(String s) {
    if (s == null) return NULL_STR;
    return s;
  }

  private String jsonEscape(String s) {
    if (s == null) return NULL_STR;
    return jsonEscapeString(s);
  }

  private void appendMDC(StringBuilder sb, ILoggingEvent event) {
    Map<String, String> map = event.getMDCPropertyMap();

    sb.append(QUOTE).append(MDC_ATTR_NAME).append(QUOTE_COL).append(SP).append(OPEN_OBJ);
    if (isNotEmptyMap(map)) {
      Set<Map.Entry<String, String>> entrySet = map.entrySet();
      int i = 0;
      for (Map.Entry<String, String> entry : entrySet) {
        if (i != 0) sb.append(VALUE_SEPARATOR);
        appenderMember(
            sb, jsonEscapedToString(entry.getKey()), jsonEscapedToString(entry.getValue()));
        i++;
      }
    }
    sb.append(CLOSE_OBJ);
    sb.append(VALUE_SEPARATOR);
  }

  boolean isNotEmptyMap(Map map) {
    if (map == null) return false;
    return !map.isEmpty();
  }
}
