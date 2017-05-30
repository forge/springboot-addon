/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.jpa;

import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.springboot.utils.CollectionStringBuffer;
import org.jboss.forge.addon.springboot.utils.SpringBootHelper;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;

import javax.inject.Inject;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class AddJNDIDatasourceCommand implements UICommand, UIWizardStep {
   @Inject
   @WithAttributes(shortName = 'd', label = "DataSource Name", required = true)
   private UIInput<String> dataSourceName;

   @Inject
   private SpringBootHelper helper;

   @Override
   public void initializeUI(UIBuilder builder) throws Exception {
      builder.add(dataSourceName);
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception {
      final CollectionStringBuffer buffer = new CollectionStringBuffer();
      buffer.append("spring.datasource.jndi-name=" + dataSourceName.getValue());

      final Project project = helper.getProject(context.getUIContext());
      SpringBootHelper.writeToApplicationProperties(project, buffer);
      return null;
   }
}
