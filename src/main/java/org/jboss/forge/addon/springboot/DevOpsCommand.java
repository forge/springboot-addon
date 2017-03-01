/**
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.springboot;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.output.UIOutput;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;

public class DevOpsCommand extends AbstractProjectCommand implements UICommand {

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
   private UIInput<String> firstName;

   @Override
   public void initializeUI(UIBuilder builder) throws Exception {
      firstName.setRequired(true);
      builder.add(firstName);
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

      UIOutput output = context.getUIContext().getProvider().getOutput();

      output.info(output.out(),"Folder : " + folder.getAbsolutePath());

      Map<Object, Object> attributeMap = context.getUIContext().getAttributeMap();
      output.info(output.out(),"Spring Boot Version : " + attributeMap.get("springboot-version"));
      output.info(output.out(),"Dependencies : " + attributeMap.get("dependencies"));

      return Results.success("Hello, " + firstName.getValue());
   }

   @Override
   public UICommandMetadata getMetadata(UIContext context) {
      return Metadata
              .forCommand(DevOpsCommand.class)
              .name("devops-command")
              .description("Fabric8 DevOps command")
              .category(Categories.create("Dev Ops"));
   }

   @Override
   public boolean isEnabled(UIContext context)
   {
      return true;
   }

}