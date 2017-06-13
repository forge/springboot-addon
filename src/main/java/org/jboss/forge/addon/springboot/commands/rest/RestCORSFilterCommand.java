/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.rest;

import org.jboss.forge.addon.parser.java.ui.AbstractJavaSourceCommand;
import org.jboss.forge.addon.parser.java.ui.JavaSourceDecorator;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;
import org.jboss.forge.roaster.model.source.JavaClassSource;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class RestCORSFilterCommand extends AbstractJavaSourceCommand<JavaClassSource> implements UIWizardStep
{
   private final AbstractJavaSourceCommand<JavaClassSource> wrapped;

   public RestCORSFilterCommand(AbstractJavaSourceCommand<JavaClassSource> wrapped)
   {
      this.wrapped = wrapped;
   }

   @Override
   public JavaClassSource decorateSource(UIExecutionContext context, Project project, JavaClassSource source)
            throws Exception
   {
      final JavaClassSource result = wrapped.decorateSource(context, project, source);
      result.addAnnotation("org.springframework.stereotype.Component");
      return result;
   }

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return wrapped.getMetadata(context);
   }

   @Override
   protected String getType()
   {
      return "Cross Origin Resource Sharing Filter";
   }

   @Override
   protected Class<JavaClassSource> getSourceType()
   {
      return JavaClassSource.class;
   }

   @Override
   public boolean isEnabled(UIContext context)
   {
      return wrapped.isEnabled(context);
   }

   @Override
   protected ProjectFactory getProjectFactory()
   {
      return null;
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      wrapped.initializeUI(builder);
   }

   @Override
   public void validate(UIValidationContext context)
   {
      wrapped.validate(context);
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      final JavaSourceDecorator<JavaClassSource> initial = wrapped.getJavaSourceDecorator();
      try
      {
         wrapped.setJavaSourceDecorator(this);
         return wrapped.execute(context);
      }
      finally
      {
         wrapped.setJavaSourceDecorator(initial);
      }
   }
}
