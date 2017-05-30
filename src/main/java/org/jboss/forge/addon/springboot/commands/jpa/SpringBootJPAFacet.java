/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.jpa;

import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.javaee.Descriptors;
import org.jboss.forge.addon.javaee.jpa.AbstractJPAFacetImpl;
import org.jboss.forge.addon.projects.dependencies.DependencyInstaller;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.springboot.SpringBootFacet;
import org.jboss.forge.furnace.versions.SingleVersion;
import org.jboss.forge.furnace.versions.Version;
import org.jboss.shrinkwrap.descriptor.api.persistence21.PersistenceDescriptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class SpringBootJPAFacet extends AbstractJPAFacetImpl<PersistenceDescriptor> {
   private final static PersistenceDescriptor DESCRIPTOR = Descriptors.create(PersistenceDescriptor.class).version("2.1");
   private final Dependency SPRING_BOOT_DATA_JPA = DependencyBuilder.create()
         .setGroupId(SpringBootFacet.SPRING_BOOT_GROUP_ID)
         .setArtifactId(SpringBootFacet.SPRING_BOOT_STARTER_DATA_JPA)
         .setScopeType("provided");


   public SpringBootJPAFacet(DependencyInstaller installer) {
      super(installer);
   }

   @Override
   public Version getSpecVersion() {
      return SingleVersion.valueOf("2.1");
   }

   @Override
   protected Map<Dependency, List<Dependency>> getRequiredDependencyOptions() {
      return Collections.singletonMap(SPRING_BOOT_DATA_JPA, Arrays.asList(SPRING_BOOT_DATA_JPA));
   }

   @Override
   public PersistenceDescriptor getConfig() {
      return DESCRIPTOR;
   }

   @Override
   protected void createDefaultConfig(FileResource<?> descriptor) {
      // do nothing
   }

   @Override
   public void saveConfig(final PersistenceDescriptor descriptor) {
      // do nothing
   }

}