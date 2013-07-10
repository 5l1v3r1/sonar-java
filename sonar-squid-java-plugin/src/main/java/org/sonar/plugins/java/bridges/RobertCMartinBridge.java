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
package org.sonar.plugins.java.bridges;

import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.Resource;
import org.sonar.squid.api.SourceFile;
import org.sonar.squid.api.SourcePackage;
import org.sonar.squid.measures.Metric;

public class RobertCMartinBridge extends Bridge {

  protected RobertCMartinBridge() {
    super(true);
  }

  @Override
  public void onPackage(SourcePackage squidPackage, Resource sonarPackage) {
    context.saveMeasure(sonarPackage, CoreMetrics.AFFERENT_COUPLINGS, squidPackage.getDouble(Metric.CA));
    context.saveMeasure(sonarPackage, CoreMetrics.EFFERENT_COUPLINGS, squidPackage.getDouble(Metric.CE));
  }

  @Override
  public void onFile(SourceFile squidFile, Resource sonarFile) {
    context.saveMeasure(sonarFile, CoreMetrics.AFFERENT_COUPLINGS, squidFile.getDouble(Metric.CA));
    context.saveMeasure(sonarFile, CoreMetrics.EFFERENT_COUPLINGS, squidFile.getDouble(Metric.CE));
  }

}
