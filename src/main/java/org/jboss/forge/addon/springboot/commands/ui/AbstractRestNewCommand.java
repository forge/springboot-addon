/**
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.springboot.commands.ui;

import javax.inject.Inject;

import org.jboss.forge.addon.javaee.rest.RestFacet;
import org.jboss.forge.addon.javaee.rest.ui.RestSetupWizard;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.ui.AbstractJavaSourceCommand;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.stacks.annotations.StackConstraint;
import org.jboss.forge.addon.ui.command.PrerequisiteCommandsProvider;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.navigation.NavigationResultBuilder;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.roaster.model.source.JavaSource;

import static org.jboss.forge.addon.javaee.JavaEEPackageConstants.DEFAULT_REST_PACKAGE;

/**
 *
 * Abstract Rest Command.
 *
 * @author <a href="cmoulliard@redhat.com">Charles Moulliard</a>
 */
//@StackConstraint(RestFacet.class)
public abstract class AbstractRestNewCommand<T extends JavaSource<?>> extends AbstractJavaSourceCommand<T>
{

   protected static final String SPRING_REST_CAT = "SPRING-REST:";

   @Inject
   private ProjectFactory projectFactory;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass())
               .category(Categories.create(Categories.create("Spring"), "MVC"));
   }

   @Override
   protected boolean isProjectRequired()
   {
      return true;
   }

   @Override
   protected String calculateDefaultPackage(UIContext context)
   {
      return getSelectedProject(context).getFacet(JavaSourceFacet.class).getBasePackage() + "."
               + DEFAULT_REST_PACKAGE;
   }

   @Override
   protected ProjectFactory getProjectFactory()
   {
      return projectFactory;
   }

}
