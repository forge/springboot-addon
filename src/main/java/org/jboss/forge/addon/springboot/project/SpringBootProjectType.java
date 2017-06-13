/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.springboot.project;

import org.jboss.forge.addon.parser.java.facets.JavaCompilerFacet;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.AbstractProjectType;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.projects.facets.*;
import org.jboss.forge.addon.projects.stacks.Stack;
import org.jboss.forge.addon.springboot.commands.setup.SetupProjectCommand;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;

import java.util.ArrayList;
import java.util.List;

public class SpringBootProjectType extends AbstractProjectType
{
   private static final List<Class<? extends ProjectFacet>> REQUIRED_FACETS = new ArrayList<>(7);

   static {
      REQUIRED_FACETS.add(MetadataFacet.class);
      REQUIRED_FACETS.add(PackagingFacet.class);
      REQUIRED_FACETS.add(DependencyFacet.class);
      REQUIRED_FACETS.add(ResourcesFacet.class);
      REQUIRED_FACETS.add(WebResourcesFacet.class);
      REQUIRED_FACETS.add(JavaSourceFacet.class);
      REQUIRED_FACETS.add(JavaCompilerFacet.class);
   }

   @Override
   public boolean supports(Stack stack)
   {
      return false;
   }

   @Override
   public String getType()
   {
      return "Spring Boot";
   }

   @Override
   public Iterable<Class<? extends ProjectFacet>> getRequiredFacets()
   {
      return REQUIRED_FACETS;
   }

   @Override
   public int priority()
   {
      return 100;
   }

   @Override
   public Class<? extends UIWizardStep> getSetupFlow()
   {
      return SetupProjectCommand.class;
   }

   @Override
   public String toString()
   {
      return "spring-boot";
   }
}
