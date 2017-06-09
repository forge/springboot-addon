/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.rest;

import org.jboss.forge.addon.javaee.rest.ui.CrossOriginResourceSharingFilterCommand;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;
import org.jboss.forge.roaster.model.source.JavaClassSource;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class RestCORSFilterCommand extends CrossOriginResourceSharingFilterCommand implements UIWizardStep {

   @Override
   public JavaClassSource decorateSource(UIExecutionContext context, Project project, JavaClassSource source) throws Exception {
      final JavaClassSource result = super.decorateSource(context, project, source);
      result.addAnnotation("org.springframework.stereotype.Component");
      return result;
   }
}
