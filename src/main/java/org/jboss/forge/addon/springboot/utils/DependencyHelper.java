/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.utils;

import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.springboot.SpringBootFacet;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class DependencyHelper {

   public static void addSpringBootDependency(Project project, String artifactId) {
      addDependency(project, SpringBootFacet.SPRING_BOOT_GROUP_ID, artifactId);
   }

   public static DependencyBuilder addDependency(Project project, String groupId, String artifactId) {
      final DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
      final DependencyBuilder dependency = DependencyBuilder.create()
            .setArtifactId(artifactId)
            .setGroupId(groupId);
      if (!dependencyFacet.hasEffectiveDependency(dependency)) {
         dependencyFacet.addDirectDependency(dependency);
      }

      return dependency;
   }
}
