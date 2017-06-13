/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.jpa;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.Entity;

import org.jboss.forge.addon.javaee.JavaEEPackageConstants;
import org.jboss.forge.addon.javaee.jpa.JPAFacet;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.parser.java.resources.JavaResourceVisitor;
import org.jboss.forge.addon.projects.dependencies.DependencyInstaller;
import org.jboss.forge.addon.projects.facets.AbstractProjectFacet;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.visit.VisitContext;
import org.jboss.forge.addon.springboot.SpringBootFacet;
import org.jboss.forge.addon.springboot.utils.SpringBootHelper;
import org.jboss.forge.furnace.versions.SingleVersion;
import org.jboss.forge.furnace.versions.Version;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.shrinkwrap.descriptor.api.DescriptorExportException;
import org.jboss.shrinkwrap.descriptor.api.persistence21.PersistenceDescriptor;
import org.jboss.shrinkwrap.descriptor.api.persistence21.PersistenceUnit;
import org.jboss.shrinkwrap.descriptor.api.persistence21.Properties;
import org.jboss.shrinkwrap.descriptor.api.persistence21.Property;
import org.jboss.shrinkwrap.descriptor.impl.persistence21.PersistenceUnitImpl;
import org.jboss.shrinkwrap.descriptor.spi.node.Node;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class SpringBootJPAFacet extends AbstractProjectFacet implements JPAFacet<PersistenceDescriptor>
{
   private final static PersistenceDescriptor DESCRIPTOR = new SpringBootPersistenceDescriptor();
   private static final String NATIVE_PROPERTIES_PREFIX = "spring.jpa.properties.";
   final static String PERSISTENCE_UNIT_NAME = "spring-boot-pu";
   public static final String SPRING_DATASOURCE_PROPERTIES_PREFIX = "spring.datasource.";

   private final DependencyInstaller installer;

   @Inject
   public SpringBootJPAFacet(final DependencyInstaller installer)
   {
      this.installer = installer;
   }

   @Override
   public String getEntityPackage()
   {
      JavaSourceFacet sourceFacet = getFaceted().getFacet(JavaSourceFacet.class);
      return sourceFacet.getBasePackage() + "." + JavaEEPackageConstants.DEFAULT_ENTITY_PACKAGE;
   }

   @Override
   public DirectoryResource getEntityPackageDir()
   {
      JavaSourceFacet sourceFacet = getFaceted().getFacet(JavaSourceFacet.class);

      DirectoryResource entityRoot = sourceFacet.getBasePackageDirectory().getChildDirectory(
               JavaEEPackageConstants.DEFAULT_ENTITY_PACKAGE);
      if (!entityRoot.exists())
      {
         entityRoot.mkdirs();
      }

      return entityRoot;
   }

   @Override
   public FileResource<?> getConfigFile()
   {
      // use application.properties instead of persistence.xml but do not create it to work correctly
      return SpringBootHelper.getApplicationProperties(getFaceted(), false);
   }

   @Override
   public List<JavaClassSource> getAllEntities()
   {
      final List<JavaClassSource> result = new ArrayList<>();
      JavaSourceFacet javaSourceFacet = getFaceted().getFacet(JavaSourceFacet.class);
      javaSourceFacet.visitJavaSources(new JavaResourceVisitor()
      {
         @Override
         public void visit(VisitContext context, JavaResource resource)
         {
            try
            {
               JavaType<?> type = resource.getJavaType();
               if (type.hasAnnotation(Entity.class) && type.isClass())
               {
                  result.add((JavaClassSource) type);
               }
            }
            catch (FileNotFoundException e)
            {
               throw new IllegalStateException(e);
            }
         }
      });

      return result;
   }

   @Override
   public PersistenceDescriptor getConfig()
   {
      return DESCRIPTOR;
   }

   @Override
   public void saveConfig(PersistenceDescriptor persistenceDescriptor)
   {
      final Properties<PersistenceUnit<PersistenceDescriptor>> properties = DESCRIPTOR.getOrCreatePersistenceUnit()
               .getOrCreateProperties();
      final List<Property<Properties<PersistenceUnit<PersistenceDescriptor>>>> propertyList = properties
               .getAllProperty();

      final java.util.Properties toWrite = new java.util.Properties();
      for (Property<Properties<PersistenceUnit<PersistenceDescriptor>>> property : propertyList)
      {
         toWrite.put(NATIVE_PROPERTIES_PREFIX + property.getName(), property.getValue());
      }

      SpringBootHelper.writeToApplicationProperties(getFaceted(), toWrite);
   }

   @Override
   public Version getSpecVersion()
   {
      return SingleVersion.valueOf("2.1");
   }

   @Override
   public String getSpecName()
   {
      return "JPA";
   }

   @Override
   public boolean install()
   {
      // create application.properties if it doesn't already exist
      SpringBootHelper.getApplicationProperties(getFaceted(), true);

      // add the SB JPA dependency
      installer.install(origin, SpringBootFacet.SPRING_BOOT_DATA_JPA);
      return true;
   }

   @Override
   public boolean isInstalled()
   {
      FileResource<?> configFile = getConfigFile();
      if (!configFile.exists())
      {
         return false;
      }
      return getSpecVersion().toString().equals(getConfig().getVersion());
   }

   private static class SpringBootPersistenceDescriptor implements PersistenceDescriptor
   {
      private final PersistenceUnit<PersistenceDescriptor> persistenceUnit = new PersistenceUnitImpl<>(this,
               PERSISTENCE_UNIT_NAME, null, new Node(PERSISTENCE_UNIT_NAME).attribute("name", PERSISTENCE_UNIT_NAME));

      @Override
      public PersistenceUnit<PersistenceDescriptor> getOrCreatePersistenceUnit()
      {
         return persistenceUnit;
      }

      @Override
      public PersistenceUnit<PersistenceDescriptor> createPersistenceUnit()
      {
         return persistenceUnit;
      }

      @Override
      public List<PersistenceUnit<PersistenceDescriptor>> getAllPersistenceUnit()
      {
         return Collections.singletonList(persistenceUnit);
      }

      @Override
      public PersistenceDescriptor removeAllPersistenceUnit()
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public PersistenceDescriptor version(String version)
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public String getVersion()
      {
         return "2.1";
      }

      @Override
      public PersistenceDescriptor removeVersion()
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public String getDescriptorName()
      {
         return "applications.properties";
      }

      @Override
      public String exportAsString() throws DescriptorExportException
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public void exportTo(OutputStream outputStream) throws DescriptorExportException, IllegalArgumentException
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public PersistenceDescriptor addDefaultNamespaces()
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public PersistenceDescriptor addNamespace(String s, String s1)
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public List<String> getNamespaces()
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public PersistenceDescriptor removeAllNamespaces()
      {
         throw new UnsupportedOperationException();
      }
   }
}
