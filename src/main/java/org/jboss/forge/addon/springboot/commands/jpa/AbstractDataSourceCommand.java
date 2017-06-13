/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.jpa;

import org.jboss.forge.addon.javaee.jpa.JPADataSource;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.springboot.utils.SpringBootHelper;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;

import javax.inject.Inject;
import java.util.Properties;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public abstract class AbstractDataSourceCommand implements UICommand, UIWizardStep {
   @Inject
   private SpringBootHelper helper;

   @Override
   public Result execute(UIExecutionContext context) throws Exception {
      final UIContext uiContext = context.getUIContext();
      JPADataSource dataSource = (JPADataSource) uiContext.getAttributeMap().get(JPADataSource.class);
      updateDataSource(dataSource);

      final Properties properties = new Properties();
      setProperties(properties, dataSource);

      final Project project = helper.getProject(uiContext);
      SpringBootHelper.writeToApplicationProperties(project, properties);

      return null;
   }

   @Override
   public NavigationResult next(UINavigationContext context) throws Exception {
      return Results.navigateTo(FinishJPASetupCommand.class);
   }

   protected abstract void updateDataSource(JPADataSource dataSource);

   protected abstract void setProperties(Properties properties, JPADataSource dataSource);
}
