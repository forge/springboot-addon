/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.springboot.utils;

import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.Projects;
import org.jboss.forge.addon.projects.dependencies.DependencyInstaller;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.springboot.SpringBootFacet;
import org.jboss.forge.addon.springboot.commands.jpa.SpringBootJPAFacet;
import org.jboss.forge.addon.ui.context.UIContext;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

public class SpringBootHelper {

   private final static String LINE_SEPARATOR = System.getProperty("line.separator");

   @Inject
   private ProjectFactory projectFactory;

   @Inject
   private FacetFactory facetFactory;

   @Inject
   private DependencyInstaller installer;


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
      final DependencyFacet dependencyFacet = project.getFacet(DependencyFacet.class);
      final DependencyBuilder dependency = DependencyBuilder.create()
            .setArtifactId(artifactId)
            .setGroupId(groupId);
      dependencyFacet.addDirectDependency(dependency);

      return dependency;
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

   public static void writeToApplicationProperties(Project project, CollectionStringBuffer lines) {
      writeToApplicationProperties(project, false, lines);
   }

   public static void writeToApplicationProperties(Project project, boolean replace, CollectionStringBuffer lines) {
      if (lines != null) {
         final FileResource<?> applicationFile = getApplicationProperties(project);

         final StringBuilder sb;
         if (!replace) {
            final String contents = applicationFile.getContents();
            sb = new StringBuilder(contents.length() + lines.size());
            sb.append(contents).append(LINE_SEPARATOR);
         } else {
            sb = new StringBuilder(lines.size());
         }

         lines.setSeparator(LINE_SEPARATOR);
         sb.append(lines);


         applicationFile.setContents(sb.toString());
      }
   }

   public Project getProject(UIContext uiContext) {
      final Project project = Projects.getSelectedProject(projectFactory, uiContext);
      if (project == null) {
         throw new IllegalStateException("A project is required in the current context");
      }

      return project;
   }

   public SpringBootJPAFacet installJPAFacet(Project project) {
      final SpringBootJPAFacet facet = new SpringBootJPAFacet(installer);
      final boolean installed = facetFactory.install(project, facet);
      if (!installed) {
         throw new RuntimeException("SpringBoot JPA Facet didn't get installed");
      }
      return facet;
   }
}
