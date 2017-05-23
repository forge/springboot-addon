/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands;

import org.jboss.forge.addon.javaee.rest.ui.RestNewEndpointCommand;
import org.jboss.forge.addon.parser.java.ui.AbstractJavaSourceCommand;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.Projects;
import org.jboss.forge.addon.springboot.SpringBootFacet;
import org.jboss.forge.addon.springboot.commands.rest.RestNewEndpointDecorator;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.command.UICommandTransformer;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A command transformer that wraps some Java EE commands in order to be able to reuse their logic and replace it
 * with Spring-related one.
 *
 * @author <a href="claprun@redhat.com>Christophe Laprun</a>
 */
@Singleton
public class SpringBootCommandTransformer implements UICommandTransformer {
   @Inject
   private ProjectFactory factory;

   @Override
   public UICommand transform(UIContext context, UICommand original) {
      final Project project = Projects.getSelectedProject(factory, context);
      if (project != null && project.hasFacet(SpringBootFacet.class)) {
         if (original instanceof org.jboss.forge.addon.javaee.rest.ui.RestNewEndpointCommand) {
            return new JavaSourceCommandWrapper((AbstractJavaSourceCommand<JavaClassSource>) original,
                  new RestNewEndpointDecorator((RestNewEndpointCommand) original));
         }
      }

      return original;
   }
}
