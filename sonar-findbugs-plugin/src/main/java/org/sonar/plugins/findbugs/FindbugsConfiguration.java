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
package org.sonar.plugins.findbugs;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.BatchExtension;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.ProjectClasspath;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.findbugs.xml.ClassFilter;
import org.sonar.plugins.findbugs.xml.FindBugsFilter;
import org.sonar.plugins.findbugs.xml.Match;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @since 2.4
 */
public class FindbugsConfiguration implements BatchExtension {

  private final Project project;
  private final Settings settings;
  private final RulesProfile profile;
  private final FindbugsProfileExporter exporter;
  private final ProjectClasspath projectClasspath;

  public FindbugsConfiguration(Project project, Settings settings, RulesProfile profile, FindbugsProfileExporter exporter, ProjectClasspath classpath) {
    this.project = project;
    this.settings = settings;
    this.profile = profile;
    this.exporter = exporter;
    this.projectClasspath = classpath;
  }

  public File getTargetXMLReport() {
    return new File(project.getFileSystem().getSonarWorkingDirectory(), "findbugs-result.xml");
  }

  public edu.umd.cs.findbugs.Project getFindbugsProject() {
    File classesDir = project.getFileSystem().getBuildOutputDir();
    if (classesDir == null || !classesDir.exists()) {
      throw new SonarException("Findbugs needs sources to be compiled. "
        + "Please build project before executing sonar and check the location of compiled classes.");
    }

    edu.umd.cs.findbugs.Project findbugsProject = new edu.umd.cs.findbugs.Project();
    for (File dir : project.getFileSystem().getSourceDirs()) {
      findbugsProject.addSourceDir(dir.getAbsolutePath());
    }
    findbugsProject.addFile(classesDir.getAbsolutePath());
    for (File file : projectClasspath.getElements()) {
      if (!file.equals(classesDir)) {
        findbugsProject.addAuxClasspathEntry(file.getAbsolutePath());
      }
    }
    findbugsProject.addAuxClasspathEntry(annotationsLib.getAbsolutePath());
    findbugsProject.addAuxClasspathEntry(jsr305Lib.getAbsolutePath());
    findbugsProject.setCurrentWorkingDirectory(project.getFileSystem().getBuildDir());
    return findbugsProject;
  }

  @VisibleForTesting
  File saveIncludeConfigXml() throws IOException {
    StringWriter conf = new StringWriter();
    exporter.exportProfile(profile, conf);
    return project.getFileSystem().writeToWorkingDirectory(conf.toString(), "findbugs-include.xml");
  }

  @VisibleForTesting
  List<File> getExcludesFilters() {
    List<File> result = new ArrayList<File>();
    String[] filters = settings.getStringArray(FindbugsConstants.EXCLUDES_FILTERS_PROPERTY);
    for (String excludesFilterPath : filters) {
      excludesFilterPath = StringUtils.trim(excludesFilterPath);
      if (StringUtils.isNotBlank(excludesFilterPath)) {
        result.add(project.getFileSystem().resolvePath(excludesFilterPath));
      }
    }
    return result;
  }

  public String getEffort() {
    return StringUtils.lowerCase(settings.getString(FindbugsConstants.EFFORT_PROPERTY));
  }

  public String getConfidenceLevel() {
    return StringUtils.lowerCase(settings.getString(FindbugsConstants.CONFIDENCE_LEVEL_PROPERTY));
  }

  public long getTimeout() {
    return settings.getLong(FindbugsConstants.TIMEOUT_PROPERTY);
  }

  public Locale getLocale() {
    return new Locale(settings.getString(CoreProperties.CORE_VIOLATION_LOCALE_PROPERTY));
  }

  private File jsr305Lib;
  private File annotationsLib;

  /**
   * Invoked by PicoContainer to extract additional FindBugs libraries into temporary files.
   */
  public void start() {
    jsr305Lib = copyLib("/jsr305-" + FindbugsVersion.getVersion() + ".jar");
    annotationsLib = copyLib("/annotations-" + FindbugsVersion.getVersion() + ".jar");
  }

  /**
   * Invoked by PicoContainer to remove temporary files.
   */
  public void stop() {
    jsr305Lib.delete();
    annotationsLib.delete();
  }

  private File copyLib(String name) {
    try {
      InputStream is = getClass().getResourceAsStream(name);
      File temp = File.createTempFile("findbugs", ".jar");
      OutputStream os = FileUtils.openOutputStream(temp);
      IOUtils.copy(is, os);
      return temp;
    } catch (IOException e) {
      throw new SonarException(e);
    }
  }

}
