/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.jpa;

import org.jboss.forge.addon.javaee.jpa.JPADataSource;
import org.jboss.forge.addon.javaee.jpa.PersistenceOperations;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.springboot.utils.SpringBootHelper;
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
public class FinishJPASetupCommand implements UICommand, UIWizardStep {
   @Inject
   private PersistenceOperations persistenceOperations;

   @Inject
   private SpringBootHelper helper;

   @Override
   public Result execute(UIExecutionContext context) throws Exception {
      final UIContext uiContext = context.getUIContext();
      final Project project = helper.getProject(uiContext);

      final Map<Object, Object> attributeMap = uiContext.getAttributeMap();
      final JPADataSource dataSource = (JPADataSource) attributeMap.get(JPADataSource.class);

      persistenceOperations.setup(SpringBootJPAFacet.PERSISTENCE_UNIT_NAME, project, dataSource, false);

      // if we're using H2, Derby or HSQL embedded databases, we need to remove any previously set data
      // source properties
      if (!SpringBootPersistenceContainer.isNotEmbeddedDB(dataSource.getDatabase())) {
         SpringBootHelper.removeSpringDataPropertiesFromApplication(project);
      }
      
      return null;
   }
}
