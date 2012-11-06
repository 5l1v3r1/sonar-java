/*
 * Sonar Java
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.hamcrest.BaseMatcher;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.StringContains.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CheckstyleExecutorTest {

  @Test
  public void execute() throws Exception {
    CheckstyleConfiguration conf = mockConf();
    CheckstyleAuditListener listener = mockListener();
    CheckstyleExecutor executor = new CheckstyleExecutor(conf, listener, getClass().getClassLoader());
    executor.execute();

    verify(listener, times(1)).auditStarted(any(AuditEvent.class));
    verify(listener, times(1)).auditFinished(any(AuditEvent.class));
    verify(listener, times(1)).fileStarted(argThat(newFilenameMatcher("Hello.java")));
    verify(listener, times(1)).fileFinished(argThat(newFilenameMatcher("Hello.java")));
    verify(listener, times(1)).fileStarted(argThat(newFilenameMatcher("World.java")));
    verify(listener, times(1)).fileFinished(argThat(newFilenameMatcher("World.java")));
    verify(listener, atLeast(1)).addError(argThat(newErrorMatcher("Hello.java", "com.puppycrawl.tools.checkstyle.checks.coding.EmptyStatementCheck")));
  }

  @Test
  public void canGenerateXMLReport() throws Exception {
    CheckstyleConfiguration conf = mockConf();
    File report = new File("target/test-tmp/checkstyle-report.xml");
    when(conf.getTargetXMLReport()).thenReturn(report);
    CheckstyleAuditListener listener = mockListener();
    CheckstyleExecutor executor = new CheckstyleExecutor(conf, listener, getClass().getClassLoader());
    executor.execute();

    assertThat(report.exists(), is(true));
    assertThat(FileUtils.readFileToString(report), containsString("<error"));
  }

  private BaseMatcher<AuditEvent> newErrorMatcher(final String filename, final String rule) {
    return new ArgumentMatcher<AuditEvent>() {
      @Override
      public boolean matches(Object o) {
        AuditEvent event = (AuditEvent) o;
        return StringUtils.endsWith(event.getFileName(), filename) && StringUtils.equals(event.getSourceName(), rule);
      }
    };
  }

  private BaseMatcher<AuditEvent> newFilenameMatcher(final String filename) {
    return new ArgumentMatcher<AuditEvent>() {
      @Override
      public boolean matches(Object o) {
        AuditEvent event = (AuditEvent) o;
        return StringUtils.endsWith(event.getFileName(), filename);
      }
    };
  }

  private CheckstyleAuditListener mockListener() {
    return mock(CheckstyleAuditListener.class);
  }

  private CheckstyleConfiguration mockConf() throws Exception {
    CheckstyleConfiguration conf = mock(CheckstyleConfiguration.class);
    when(conf.getCharset()).thenReturn(Charset.defaultCharset());
    when(conf.getCheckstyleConfiguration()).thenReturn(CheckstyleConfiguration.toCheckstyleConfiguration(new File("test-resources/checkstyle-conf.xml")));
    when(conf.getSourceFiles()).thenReturn(Arrays.<File> asList(new File("test-resources/Hello.java"), new File("test-resources/World.java")));
    when(conf.getLocale()).thenReturn(Locale.ENGLISH);
    return conf;
  }
}
