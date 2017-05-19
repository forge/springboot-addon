/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.ui;

import org.jboss.forge.addon.parser.java.ui.AbstractJavaSourceCommand;
import org.jboss.forge.addon.parser.java.ui.JavaSourceDecorator;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.roaster.model.source.JavaClassSource;

/**
 * Wraps an existing {@link AbstractJavaSourceCommand} so that its code decoration logic can be replaced by the one
 * provided by the specified {@link JavaSourceDecorator}.
 *
 * @author <a href="claprun@redhat.com>Christophe Laprun</a>
 */
public class JavaSourceCommandWrapper implements UICommand {
   private final AbstractJavaSourceCommand<JavaClassSource> wrapped;
   private final JavaSourceDecorator<JavaClassSource> decorator;

   public JavaSourceCommandWrapper(AbstractJavaSourceCommand<JavaClassSource> wrapped, JavaSourceDecorator<JavaClassSource> decorator) {
      this.wrapped = wrapped;
      this.decorator = decorator;
   }

   @Override
   public boolean isEnabled(UIContext context) {
      return wrapped.isEnabled(context);
   }

   @Override
   public UICommandMetadata getMetadata(UIContext context) {
      return wrapped.getMetadata(context);
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception {
      wrapped.initializeUI(builder);
   }

   /**
    * Executes the wrapped command logic, temporarily using the provided {@link JavaSourceDecorator} instance before
    * resetting it to what it previously was before the invocation.
    */
   @Override
   public Result execute(UIExecutionContext context) throws Exception {
      final JavaSourceDecorator<JavaClassSource> initial = wrapped.getJavaSourceDecorator();
      try {
         wrapped.setJavaSourceDecorator(decorator);
         return wrapped.execute(context);
      } finally {
         wrapped.setJavaSourceDecorator(initial);
      }
   }

   @Override
   public void validate(UIValidationContext validator) {
      wrapped.validate(validator);
   }
}
