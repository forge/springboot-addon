/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.jpa;

import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.springboot.SpringBootFacet;
import org.jboss.forge.addon.springboot.utils.SpringBootHelper;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;

import javax.inject.Inject;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class AddJPADependencyCommand implements UICommand, UIWizardStep {
   @Inject
   private SpringBootHelper helper;

   @Override
   public Result execute(UIExecutionContext context) throws Exception {
      // Check that we have the spring-boot-starter-web dependency and add it if we don't
      final UIContext uiContext = context.getUIContext();
      final Project project = helper.getProject(uiContext);

      SpringBootHelper.addSpringBootDependency(project, SpringBootFacet.SPRING_BOOT_STARTER_DATA_JPA);

      return null;
   }
}
