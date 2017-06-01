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
import org.jboss.forge.addon.javaee.jpa.AbstractJPAFacetImpl;
import org.jboss.forge.addon.projects.dependencies.DependencyInstaller;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.springboot.SpringBootFacet;
import org.jboss.forge.addon.springboot.utils.CollectionStringBuffer;
import org.jboss.forge.addon.springboot.utils.SpringBootHelper;
import org.jboss.forge.furnace.versions.SingleVersion;
import org.jboss.forge.furnace.versions.Version;
import org.jboss.shrinkwrap.descriptor.api.DescriptorExportException;
import org.jboss.shrinkwrap.descriptor.api.persistence21.PersistenceDescriptor;
import org.jboss.shrinkwrap.descriptor.api.persistence21.PersistenceUnit;
import org.jboss.shrinkwrap.descriptor.api.persistence21.Properties;
import org.jboss.shrinkwrap.descriptor.api.persistence21.Property;
import org.jboss.shrinkwrap.descriptor.impl.persistence21.PersistenceUnitImpl;
import org.jboss.shrinkwrap.descriptor.spi.node.Node;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class SpringBootJPAFacet extends AbstractJPAFacetImpl<PersistenceDescriptor> {
   private final static PersistenceDescriptor DESCRIPTOR = new SpringBootPersistenceDescriptor();
   private static final String NATIVE_PROPERTIES_PREFIX = "spring.jpa.properties.";
   private final Dependency SPRING_BOOT_DATA_JPA = DependencyBuilder.create()
         .setGroupId(SpringBootFacet.SPRING_BOOT_GROUP_ID)
         .setArtifactId(SpringBootFacet.SPRING_BOOT_STARTER_DATA_JPA)
         .setScopeType("provided");
   final static String PERSISTENCE_UNIT_NAME = "spring-boot-pu";

   public SpringBootJPAFacet(DependencyInstaller installer) {
      super(installer);
   }

   @Override
   public FileResource<?> getConfigFile() {
      // use application.properties instead of persistence.xml
      return SpringBootHelper.getApplicationProperties(getFaceted(), false);
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
      final CollectionStringBuffer buffer = new CollectionStringBuffer();

      final Properties<PersistenceUnit<PersistenceDescriptor>> properties = DESCRIPTOR.getOrCreatePersistenceUnit().getOrCreateProperties();
      final List<Property<Properties<PersistenceUnit<PersistenceDescriptor>>>> propertyList = properties.getAllProperty();
      for (Property<Properties<PersistenceUnit<PersistenceDescriptor>>> property : propertyList) {
         buffer.append(NATIVE_PROPERTIES_PREFIX + property.getName() + "=" + property.getValue());
      }

      SpringBootHelper.writeToApplicationProperties(getFaceted(), buffer);
   }


   private static class SpringBootPersistenceDescriptor implements PersistenceDescriptor {
      private final PersistenceUnit<PersistenceDescriptor> persistenceUnit =
            new PersistenceUnitImpl<>(this, PERSISTENCE_UNIT_NAME, null, new Node(PERSISTENCE_UNIT_NAME).attribute
                  ("name", PERSISTENCE_UNIT_NAME));

      @Override
      public PersistenceUnit<PersistenceDescriptor> getOrCreatePersistenceUnit() {
         return persistenceUnit;
      }

      @Override
      public PersistenceUnit<PersistenceDescriptor> createPersistenceUnit() {
         return persistenceUnit;
      }

      @Override
      public List<PersistenceUnit<PersistenceDescriptor>> getAllPersistenceUnit() {
         return Collections.singletonList(persistenceUnit);
      }

      @Override
      public PersistenceDescriptor removeAllPersistenceUnit() {
         throw new UnsupportedOperationException();
      }

      @Override
      public PersistenceDescriptor version(String version) {
         throw new UnsupportedOperationException();
      }

      @Override
      public String getVersion() {
         return "2.1";
      }

      @Override
      public PersistenceDescriptor removeVersion() {
         throw new UnsupportedOperationException();
      }

      @Override
      public String getDescriptorName() {
         return "applications.properties";
      }

      @Override
      public String exportAsString() throws DescriptorExportException {
         throw new UnsupportedOperationException();
      }

      @Override
      public void exportTo(OutputStream outputStream) throws DescriptorExportException, IllegalArgumentException {
         throw new UnsupportedOperationException();
      }

      @Override
      public PersistenceDescriptor addDefaultNamespaces() {
         throw new UnsupportedOperationException();
      }

      @Override
      public PersistenceDescriptor addNamespace(String s, String s1) {
         throw new UnsupportedOperationException();
      }

      @Override
      public List<String> getNamespaces() {
         throw new UnsupportedOperationException();
      }

      @Override
      public PersistenceDescriptor removeAllNamespaces() {
         throw new UnsupportedOperationException();
      }
   }
}