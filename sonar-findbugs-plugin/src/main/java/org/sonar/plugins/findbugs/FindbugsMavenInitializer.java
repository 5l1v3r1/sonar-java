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
package org.sonar.plugins.findbugs;

import org.apache.commons.configuration.Configuration;
import org.sonar.api.batch.Initializer;
import org.sonar.api.batch.SupportedEnvironment;
import org.sonar.api.batch.maven.MavenPlugin;
import org.sonar.api.batch.maven.MavenUtils;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.Project;

/**
 * Configures Sonar FindBugs Plugin according to configuration of findbugs-maven-plugin.
 * Supports only "excludeFilterFile".
 * 
 * @since 2.10
 */
@SupportedEnvironment("maven")
public class FindbugsMavenInitializer extends Initializer {

  private static final String FINDBUGS_GROUP_ID = MavenUtils.GROUP_ID_CODEHAUS_MOJO;
  private static final String FINDBUGS_ARTIFACT_ID = "findbugs-maven-plugin";

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return Java.KEY.equals(project.getLanguageKey())
      && !project.getFileSystem().mainFiles(Java.KEY).isEmpty();
  }

  @Override
  public void execute(Project project) {
    Configuration conf = project.getConfiguration();
    if (!conf.containsKey(FindbugsConstants.EXCLUDES_FILTERS_PROPERTY)) {
      conf.setProperty(FindbugsConstants.EXCLUDES_FILTERS_PROPERTY, getExcludesFiltersFromPluginConfiguration(project));
    }
  }

  private static String getExcludesFiltersFromPluginConfiguration(Project project) {
    MavenPlugin mavenPlugin = MavenPlugin.getPlugin(project.getPom(), FINDBUGS_GROUP_ID, FINDBUGS_ARTIFACT_ID);
    return mavenPlugin != null ? mavenPlugin.getParameter("excludeFilterFile") : null;
  }

}
