/**
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.springboot;

import java.io.File;

import javax.inject.Inject;

import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

public class SpringBootCommand extends AbstractProjectCommand implements UICommand  {

   @Inject
   private ProjectFactory projectFactory;

   @Override
   protected boolean isProjectRequired() {
      return false;
   }

   @Override
   public ProjectFactory getProjectFactory() {
      return projectFactory;
   }

   @Inject
   private UIInput<String> lastName;

   @Override
   public void initializeUI(UIBuilder builder) throws Exception {
      lastName.setRequired(true);
      builder.add(lastName);
   }

   @Override
   public void validate(UIValidationContext context) { }

   @Override
   public Result execute(UIExecutionContext context) throws Exception {
      UIContext uiContext = context.getUIContext();
      Project project = (Project) uiContext.getAttributeMap().get(Project.class);
      if (project == null) {
         project = getSelectedProject(context.getUIContext());
      }
      File folder = project.getRoot().reify(DirectoryResource.class).getUnderlyingResourceObject();
      System.out.println("Folder: " + folder.getAbsolutePath());
      return Results.success("Goodbye, " + lastName.getValue());
   }

   @Override
   public UICommandMetadata getMetadata(UIContext context) {
      return Metadata
              .forCommand(SpringBootCommand.class)
              .name("spring-boot-command")
              .description("generic test command")
              .category(Categories.create("Spring Boot"));
   }

   @Override
   public boolean isEnabled(UIContext context)
   {
      return true;
   }

}