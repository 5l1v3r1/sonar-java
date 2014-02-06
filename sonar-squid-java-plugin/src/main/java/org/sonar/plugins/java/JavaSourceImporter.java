/*
 * SonarQube Java
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
package org.sonar.plugins.java;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.DependedUpon;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.JavaFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.utils.SonarException;
import org.sonar.java.api.JavaUtils;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @deprecated useless for SQ 4.2 (SONARJAVA-438)
 */
@Deprecated
@Phase(name = Phase.Name.PRE)
@DependedUpon(JavaUtils.BARRIER_BEFORE_SQUID)
public final class JavaSourceImporter implements Sensor {

  private boolean importSources = false;

  public JavaSourceImporter(Configuration conf) {
    this.importSources = conf.getBoolean(CoreProperties.CORE_IMPORT_SOURCES_PROPERTY, CoreProperties.CORE_IMPORT_SOURCES_DEFAULT_VALUE);
  }

  JavaSourceImporter(boolean importSources) {
    this.importSources = importSources;
  }

  public boolean shouldExecuteOnProject(Project project) {
    return Java.KEY.equals(project.getLanguageKey());
  }

  public void analyse(Project project, SensorContext context) {
    analyse(project.getFileSystem(), context);
  }

  void analyse(ProjectFileSystem fileSystem, SensorContext context) {
    parseDirs(context, fileSystem.mainFiles(Java.KEY), false, fileSystem.getSourceCharset());
    parseDirs(context, fileSystem.testFiles(Java.KEY), true, fileSystem.getSourceCharset());
  }

  void parseDirs(SensorContext context, List<InputFile> inputFiles, boolean unitTest, Charset sourcesEncoding) {
    for (InputFile inputFile : inputFiles) {
      JavaFile javaFile = JavaFile.fromRelativePath(inputFile.getRelativePath(), unitTest);
      importSource(context, javaFile, inputFile, sourcesEncoding);
    }
  }

  void importSource(SensorContext context, JavaFile javaFile, InputFile inputFile, Charset sourcesEncoding) {
    String source = null;
    if (importSources) {
      source = loadSourceFromFile(inputFile, sourcesEncoding);
    }

    try {
      context.index(javaFile);
      if (source != null) {
        context.saveSource(javaFile, source);
      }
    } catch (Exception e) {
      throw new SonarException("Unable to read and import the source file : '" + inputFile.getFile().getAbsolutePath() + "' with the charset : '"
        + sourcesEncoding.name() + "'.", e);
    }
  }

  protected String loadSourceFromFile(InputFile inputFile, Charset sourcesEncoding) {
    try {
      return FileUtils.readFileToString(inputFile.getFile(), sourcesEncoding.name());
    } catch (Exception e) {
      throw new SonarException("Unable to read and import the source file : '" + inputFile.getFile().getAbsolutePath() + "' with the charset : '"
        + sourcesEncoding.name() + "'.", e);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
