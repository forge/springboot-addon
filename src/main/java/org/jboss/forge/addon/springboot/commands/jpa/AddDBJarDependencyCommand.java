/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.jpa;

import org.jboss.forge.addon.javaee.jpa.JPADataSource;
import org.jboss.forge.addon.javaee.jpa.JPAFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.Projects;
import org.jboss.forge.addon.springboot.utils.DependencyHelper;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;

import javax.inject.Inject;
import java.util.Map;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class AddDBJarDependencyCommand implements UICommand, UIWizardStep {
   @Inject
   private ProjectFactory factory;

   @Override
   public Result execute(UIExecutionContext context) throws Exception {
      final UIContext uiContext = context.getUIContext();
      final Project project = Projects.getSelectedProject(factory, uiContext);
      if (project == null) {
         throw new IllegalStateException("A project is required in the current context");
      }

      final Map<Object, Object> attributeMap = uiContext.getAttributeMap();
      final JPADataSource dataSource = (JPADataSource) attributeMap.get(JPADataSource.class);
      switch (dataSource.getDatabase()) {
         case H2:
            DependencyHelper.addDependency(project, "com.h2database", "h2").setScopeType("runtime");
            break;
         case DERBY:
            DependencyHelper.addDependency(project, "org.apache.derby", "derby").setScopeType("runtime");
            break;
         case HSQLDB:
            DependencyHelper.addDependency(project, "org.hsqldb", "hsqldb").setScopeType("runtime");
            break;
      }

      // erase persistence.xml
      project.getFacet(JPAFacet.class).getConfigFile().delete();

      return null;
   }
}
