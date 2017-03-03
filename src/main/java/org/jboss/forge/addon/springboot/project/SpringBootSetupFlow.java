/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.springboot.project;

import org.jboss.forge.addon.springboot.commands.SetupProjectCommand;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.result.navigation.NavigationResultBuilder;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;

public class SpringBootSetupFlow implements UIWizardStep
{

   @Override
   public NavigationResult next(UINavigationContext context) throws Exception
   {
      return NavigationResultBuilder.create().add(SetupProjectCommand.class).build();
   }

   @Override
   public Result execute(UIExecutionContext uiExecutionContext) throws Exception
   {
      return Results.success();
   }
}
