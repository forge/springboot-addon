/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.springboot.utils;

import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.Projects;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.springboot.SpringBootFacet;
import org.jboss.forge.addon.springboot.commands.jpa.SpringBootJPAFacet;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class SpringBootHelper {

   private final static String LINE_SEPARATOR = System.getProperty("line.separator");

   @Inject
   private ProjectFactory projectFactory;

   @Inject
   private FacetFactory facetFactory;

   @Inject
   private SpringBootJPAFacet jpaFacet;

   public static String getVersion(String name) {
      try (InputStream is = SpringBootHelper.class
            .getResourceAsStream("/META-INF/maven/org.jboss.forge.addon/spring-boot/pom.xml")) {
         String xml = IOHelper.loadText(is);
         String version = between(xml, "<" + name + ">", "</" + name + ">");
         return version;
      } catch (IOException e) {
         // ignore
      }
      return "";
   }

   /**
    * Returns the string after the given token
    *
    * @param text  the text
    * @param after the token
    * @return the text after the token, or <tt>null</tt> if text does not contain the token
    */
   public static String after(String text, String after) {
      if (!text.contains(after)) {
         return null;
      }
      return text.substring(text.indexOf(after) + after.length());
   }

   /**
    * Returns the string before the given token
    *
    * @param text   the text
    * @param before the token
    * @return the text before the token, or <tt>null</tt> if text does not contain the token
    */
   public static String before(String text, String before) {
      if (!text.contains(before)) {
         return null;
      }
      return text.substring(0, text.indexOf(before));
   }

   /**
    * Returns the string between the given tokens
    *
    * @param text   the text
    * @param after  the before token
    * @param before the after token
    * @return the text between the tokens, or <tt>null</tt> if text does not contain the tokens
    */
   public static String between(String text, String after, String before) {
      text = after(text, after);
      if (text == null) {
         return null;
      }
      return before(text, before);
   }

   public static DependencyBuilder addSpringBootDependency(Project project, String artifactId) {
      return addDependency(project, SpringBootFacet.SPRING_BOOT_GROUP_ID, artifactId);
   }

   public static DependencyBuilder addDependency(Project project, String groupId, String artifactId) {
      return addDependency(project, DependencyBuilder.create().setArtifactId(artifactId).setGroupId(groupId));
   }

   public static DependencyBuilder addDependency(Project project, Dependency dependency) {
      final DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
      final DependencyBuilder builder = dependency instanceof DependencyBuilder
            ? (DependencyBuilder) dependency : DependencyBuilder.create(dependency);

      dependencyFacet.addDirectDependency(builder);

      return builder;
   }

   /**
    * Returns (and creates if needed) the `application.properties` file associated with the specified project.
    *
    * @param project the {@link Project} for which we want to retrieve the application properties
    * @return the `application.properties` file associated with the specified project, creating it if it doesn't
    * already exists.
    */
   public static FileResource<?> getApplicationProperties(Project project) {
      return getApplicationProperties(project, true);
   }

   public static FileResource<?> getApplicationProperties(Project project, boolean create) {
      FileResource<?> applicationFile = project.getFacet(ResourcesFacet.class).getResource("application.properties");
      if (create && !applicationFile.exists()) {
         applicationFile.createNewFile();
      }

      return applicationFile;
   }

   public static void writeToApplicationProperties(Project project, Properties properties) {
      writeToApplicationProperties(project, false, properties);
   }

   public static void writeToApplicationProperties(Project project, boolean replaceCompletely, Properties properties) {
      if (properties != null) {
         final FileResource<?> applicationFile = getApplicationProperties(project);

         final Properties newProps;
         if (!replaceCompletely) {
            // load existing properties and merge them with the new ones, replacing old values with new ones when the
            // new properties provide values for already existing keys
            try (InputStream inStream = applicationFile.getResourceInputStream()) {
               newProps = new Properties();
               newProps.load(inStream);

               for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                  newProps.put(entry.getKey(), entry.getValue());
               }


            } catch (IOException e) {
               throw new RuntimeException("Couldn't read existing " + applicationFile.getFullyQualifiedName(), e);
            }

         } else {
            newProps = properties;
         }

         try (OutputStream out = applicationFile.getResourceOutputStream()) {
            newProps.store(out, null);
         } catch (IOException e) {
            throw new RuntimeException("Couldn't save " + applicationFile.getFullyQualifiedName(), e);
         }
      }
   }

   public static void removeSpringDataPropertiesFromApplication(Project project) {
      final FileResource<?> applicationFile = getApplicationProperties(project);

      Properties initial = new Properties();
      Properties newProps = new Properties();
      try (InputStream inStream = applicationFile.getResourceInputStream()) {
         initial.load(inStream);

         for (Map.Entry<Object, Object> entry : initial.entrySet()) {
            final Object key = entry.getKey();
            if (!key.toString().startsWith(SpringBootJPAFacet.SPRING_DATASOURCE_PROPERTIES_PREFIX)) {
               newProps.put(key, entry.getValue());
            }
         }

      } catch (IOException e) {
         throw new RuntimeException("Couldn't read existing " + applicationFile.getFullyQualifiedName(), e);
      }

      try (OutputStream out = applicationFile.getResourceOutputStream()) {
         newProps.store(out, null);
      } catch (IOException e) {
         throw new RuntimeException("Couldn't save " + applicationFile.getFullyQualifiedName(), e);
      }
   }

   public Project getProject(UIContext uiContext) {
      return Projects.getSelectedProject(projectFactory, uiContext);
   }

   public SpringBootJPAFacet installJPAFacet(Project project) {
      final boolean installed = facetFactory.install(project, jpaFacet);
      if (!installed) {
         throw new RuntimeException("SpringBoot JPA Facet didn't get installed");
      }
      return jpaFacet;
   }

   public static void modifyJavaClass(Project project, String className, Optional<String> packageName,
                                      JavaClassSourceDecorator decorator) {

      final String packageLocation = packageName.orElseGet(() -> project.getFacet(MetadataFacet.class).getProjectGroupName());
      final JavaSourceFacet sourceFacet = project.getFacet(JavaSourceFacet.class);
      final DirectoryResource targetPackage = sourceFacet.getPackage(packageLocation);

      final JavaResource sbAppResource = targetPackage.getChild(className).as(JavaResource.class);
      if (sbAppResource.exists()) {
         JavaClassSource sbApp = Roaster.parse(JavaClassSource.class, sbAppResource.getResourceInputStream());

         decorator.modify(sbApp);

         sourceFacet.saveJavaSource(sbApp);
      }
   }

   public static void modifySpringBootApplication(Project project, JavaClassSourceDecorator decorator) {
      // todo: find a better way than hardcode app name, maybe iterate over files and look for @SpringBootApplication
      modifyJavaClass(project, "DemoApplication.java", Optional.empty(), decorator);
   }

   @FunctionalInterface
   public interface JavaClassSourceDecorator {
      JavaClassSource modify(JavaClassSource source);
   }
}
