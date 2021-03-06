/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.rest;

import java.util.Properties;

import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.springboot.SpringBootFacet;
import org.jboss.forge.addon.springboot.utils.SpringBootHelper;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class RestGenerateFromEntitiesCommand implements UICommand, UIWizardStep
{
   private final UICommand original;
   private final SpringBootHelper helper;

   public RestGenerateFromEntitiesCommand(UICommand original, SpringBootHelper helper)
   {
      this.original = original;
      this.helper = helper;
   }

   @Override
   public boolean isEnabled(UIContext context)
   {
      return original.isEnabled(context);
   }

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return original.getMetadata(context);
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      original.initializeUI(builder);
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      final Result result = original.execute(context);

      final Project project = helper.getProject(context.getUIContext());
      final DependencyFacet facet = project.getFacet(DependencyFacet.class);

      // add dependencies
      facet.addDirectDependency(SpringBootFacet.SPRING_BOOT_STARTER_WEB);
      facet.addDirectDependency(SpringBootFacet.CXF_SPRING_BOOT);
      facet.addDirectDependency(SpringBootFacet.JACKSON_JAXRS_PROVIDER);

      // remove EE dependencies
      facet.removeDependency(SpringBootFacet.JBOSS_EJB_SPEC);
      facet.removeDependency(SpringBootFacet.JBOSS_JAXRS_SPEC);
      facet.removeDependency(SpringBootFacet.JBOSS_SERVLET_SPEC);
      facet.removeManagedDependency(SpringBootFacet.JBOSS_EE_SPEC);

      // add CXF properties to application.properties
      final Properties cxfProps = new Properties();
      cxfProps.put("cxf.jaxrs.component-scan", "true");
      cxfProps.put("cxf.path", "/rest");
      SpringBootHelper.writeToApplicationProperties(project, cxfProps);

      SpringBootHelper.modifySpringBootApplication(project, sbApp -> {
         sbApp.addImport("com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider");

         final MethodSource<JavaClassSource> method = sbApp.addMethod("public JacksonJsonProvider config() {\n" +
                  "\t\treturn new JacksonJsonProvider();\n" +
                  "\t}");
         method.addAnnotation("org.springframework.context.annotation.Bean");

         return sbApp;
      });

      return result;
   }
}
