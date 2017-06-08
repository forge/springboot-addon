/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot;

import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.projects.facets.AbstractProjectFacet;
import org.jboss.forge.addon.projects.facets.DependencyFacet;

/**
 * A {@link org.jboss.forge.addon.projects.ProjectFacet} to mark a project as a Spring Boot one.
 *
 * @author <a href="claprun@redhat.com>Christophe Laprun</a>
 */
public class SpringBootFacet extends AbstractProjectFacet {
   public static final String SPRING_BOOT_STARTER_WEB = "spring-boot-starter-web";
   public static final String SPRING_BOOT_STARTER_DATA_JPA = "spring-boot-starter-data-jpa";
   public static final String SPRING_BOOT_GROUP_ID = "org.springframework.boot";
   private static final String SPRING_BOOT_STARTER_ARTIFACT = "spring-boot-starter";
   private static final Dependency SPRING_BOOT_STARTER = DependencyBuilder.create()
         .setGroupId(SPRING_BOOT_GROUP_ID)
         .setArtifactId(SPRING_BOOT_STARTER_ARTIFACT);

   // dependencies for REST

   public static final Dependency CXF_SPRING_BOOT = DependencyBuilder.create()
         .setGroupId("org.apache.cxf")
         .setArtifactId("cxf-spring-boot-starter-jaxrs")
         .setVersion("3.1.11");
   public static final Dependency JACKSON_JAXRS_PROVIDER = DependencyBuilder.create()
         .setGroupId("com.fasterxml.jackson.jaxrs")
         .setArtifactId("jackson-jaxrs-json-provider");

   // dependencies added by EE Forge commands to remove

   public static final Dependency JBOSS_SERVLET_SPEC = DependencyBuilder.create()
         .setGroupId("org.jboss.spec.javax.servlet")
         .setArtifactId("jboss-servlet-api_3.0_spec");
   public static final Dependency JBOSS_JAXRS_SPEC = DependencyBuilder.create()
         .setGroupId("org.jboss.spec.javax.ws.rs")
         .setArtifactId("jboss-jaxrs-api_1.1_spec");
   public static final Dependency JBOSS_EJB_SPEC = DependencyBuilder.create()
         .setGroupId("org.jboss.spec.javax.ejb")
         .setArtifactId("jboss-ejb-api_3.1_spec");
   public static final Dependency JBOSS_EE_SPEC = DependencyBuilder.create()
         .setGroupId("org.jboss.spec")
         .setArtifactId("jboss-javaee-6.0")
         .setPackaging("pom");


   @Override
   public boolean install() {
      return true;
   }

   @Override
   public boolean isInstalled() {
      final DependencyFacet facet = getFaceted().getFacet(DependencyFacet.class);
      return facet.hasDirectDependency(SPRING_BOOT_STARTER);
   }
}
