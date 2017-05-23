/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot;

import org.jboss.forge.addon.projects.facets.AbstractProjectFacet;

/**
 * A {@link org.jboss.forge.addon.projects.ProjectFacet} to mark a project as a Spring Boot one.
 *
 * @author <a href="claprun@redhat.com>Christophe Laprun</a>
 */
public class SpringBootFacet extends AbstractProjectFacet {
   public static final String SPRING_BOOT_STARTER_WEB = "spring-boot-starter-web";
   public static final String SPRING_BOOT_GROUP_ID = "org.springframework.boot";

   @Override
   public boolean install() {
      return true;
   }

   @Override
   public boolean isInstalled() {
      return true;
   }
}
