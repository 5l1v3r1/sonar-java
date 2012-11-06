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

import com.google.common.collect.ImmutableList;
import org.sonar.api.Extension;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;

import java.util.List;

@Properties({
  @Property(key = CheckstyleConstants.FILTERS_KEY,
    defaultValue = CheckstyleConstants.FILTERS_DEFAULT_VALUE,
    name = "Filters",
    description = "Checkstyle support three error filtering mechanisms : SuppressionCommentFilter, SuppressWithNearbyCommentFilter and SuppressionFilter."
      + "This property allows to configure all those filters with a native XML format."
      + " See <a href='http://checkstyle.sourceforge.net/config.html'>Checkstyle configuration page</a> to get more information on those filters.",
    project = false,
    global = true,
    type = PropertyType.TEXT)})
public final class CheckstylePlugin extends SonarPlugin {

  public List<Class<? extends Extension>> getExtensions() {
    return ImmutableList.of(
        CheckstyleSensor.class,
        CheckstyleConfiguration.class,
        CheckstyleExecutor.class,
        CheckstyleAuditListener.class,
        CheckstyleProfileExporter.class,
        CheckstyleProfileImporter.class,
        CheckstyleRuleRepository.class,
        SonarWayProfile.class,
        SunConventionsProfile.class,
        SonarWayWithFindbugsProfile.class);
  }
}
