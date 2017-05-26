/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.jpa;

import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.javaee.jpa.DatabaseType;
import org.jboss.forge.addon.javaee.jpa.JPADataSource;
import org.jboss.forge.addon.javaee.jpa.JPAFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.Projects;
import org.jboss.forge.addon.springboot.utils.CollectionStringBuffer;
import org.jboss.forge.addon.springboot.utils.SpringBootHelper;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;
import org.springframework.boot.jdbc.DatabaseDriver;

import javax.inject.Inject;
import java.util.Map;

import static org.jboss.forge.addon.javaee.jpa.DatabaseType.*;

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
      final DatabaseType database = dataSource.getDatabase();
      final Coordinate driverCoordinate = database.getDriverCoordinate();
      // add driver dependency
      SpringBootHelper.addDependency(project, driverCoordinate.getGroupId(), driverCoordinate.getArtifactId())
            .setScopeType("runtime");
      // if we're not using H2, Derby or HSQL embedded databases, we also need to add DB info in application.properties
      if (!database.equals(H2) && !database.equals(DERBY) && !database.equals(HSQLDB)) {

         final DatabaseDriver driver = DatabaseDriver.fromProductName(database.name());
         if (driver.equals(DatabaseDriver.UNKNOWN)) {
            // Spring Boot doesn't know about this DB
            throw new IllegalStateException("Unknown DB: " + dataSource);
         }

         final CollectionStringBuffer buffer = new CollectionStringBuffer();
         final String jndiDataSource = dataSource.getJndiDataSource();
         if (jndiDataSource != null) {
            buffer.append("spring.datasource.jndi-name=" + jndiDataSource);
         } else {
            final String databaseURL = dataSource.getDatabaseURL();
            if (databaseURL != null) {
               buffer.append("spring.datasource.url=" + databaseURL);
            } else {
               throw new IllegalStateException("Must provide either a JNDI data source or a database connection URL!");
            }
         }

         SpringBootHelper.writeToApplicationProperties(project, buffer);
      }

      // erase persistence.xml
      project.getFacet(JPAFacet.class).getConfigFile().delete();

      return null;
   }
}
