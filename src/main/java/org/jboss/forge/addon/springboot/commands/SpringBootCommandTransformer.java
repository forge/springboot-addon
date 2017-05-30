/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands;

import org.jboss.forge.addon.javaee.jpa.ui.JPANewEntityCommand;
import org.jboss.forge.addon.javaee.jpa.ui.setup.JPASetupWizard;
import org.jboss.forge.addon.javaee.rest.ui.RestNewEndpointCommand;
import org.jboss.forge.addon.parser.java.ui.AbstractJavaSourceCommand;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.springboot.SpringBootFacet;
import org.jboss.forge.addon.springboot.commands.jpa.CreateSpringBootJPASupportDecorator;
import org.jboss.forge.addon.springboot.commands.jpa.SpringBootJPASetupWizard;
import org.jboss.forge.addon.springboot.commands.rest.RestNewEndpointDecorator;
import org.jboss.forge.addon.springboot.utils.SpringBootHelper;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.command.UICommandTransformer;
import org.jboss.forge.addon.ui.context.UIContext;

import javax.enterprise.inject.Instance;
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
   private SpringBootHelper helper;

   @Inject
   Instance<SpringBootJPASetupWizard> jpaSetupWizard;

   @Override
   public UICommand transform(UIContext context, UICommand original) {
      final Project project = helper.getProject(context);
      if (project != null && project.hasFacet(SpringBootFacet.class)) {
         if (original instanceof org.jboss.forge.addon.javaee.rest.ui.RestNewEndpointCommand) {
            return JavaSourceCommandWrapper.wrap(original,
                  new RestNewEndpointDecorator((RestNewEndpointCommand) original));
         }

         if (original instanceof JPASetupWizard) {
            return jpaSetupWizard.get();
         }

         if (original instanceof JPANewEntityCommand) {
            return JavaSourceCommandWrapper.wrap(original,
                  new CreateSpringBootJPASupportDecorator((AbstractJavaSourceCommand) original));

         }
      }

      return original;
   }
}
