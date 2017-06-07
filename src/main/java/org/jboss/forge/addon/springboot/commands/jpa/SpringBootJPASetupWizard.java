/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.jpa;

import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.javaee.jpa.*;
import org.jboss.forge.addon.javaee.jpa.providers.HibernateProvider;
import org.jboss.forge.addon.javaee.jpa.ui.setup.JPASetupWizard;
import org.jboss.forge.addon.javaee.ui.AbstractJavaEECommand;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.stacks.annotations.StackConstraint;
import org.jboss.forge.addon.springboot.SpringBootFacet;
import org.jboss.forge.addon.springboot.utils.SpringBootHelper;
import org.jboss.forge.addon.ui.context.*;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.springframework.boot.jdbc.DatabaseDriver;

import javax.inject.Inject;
import java.util.Map;

import static org.jboss.forge.addon.javaee.jpa.DatabaseType.H2;
import static org.jboss.forge.addon.springboot.commands.jpa.SpringBootPersistenceContainer.isNotEmbeddedDB;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
@FacetConstraint(JavaSourceFacet.class)
@StackConstraint(JPAFacet.class)
public class SpringBootJPASetupWizard extends AbstractJavaEECommand implements JPASetupWizard {
   @Inject
   @WithAttributes(shortName = 't', label = "Database Type", required = true)
   private UISelectOne<DatabaseType> dbType;

   @Inject
   @WithAttributes(shortName = 'd', label = "DataSource Name")
   private UIInput<String> dataSourceName;

   @Inject
   @WithAttributes(shortName = 'u', label = "Database URL")
   private UIInput<String> databaseURL;

   @Inject
   @WithAttributes(shortName = 'p', label = "Provider", required = true)
   private UISelectOne<PersistenceProvider> jpaProvider;

   @Inject
   private HibernateProvider defaultProvider;

   @Inject
   private SpringBootHelper helper;

   @Override
   public Metadata getMetadata(UIContext context) {
      return Metadata.from(super.getMetadata(context), getClass()).name("JPA: Setup")
            .description("Setup JPA in your project")
            .category(Categories.create(super.getMetadata(context).getCategory().getName(), "JPA"));
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception {
      dbType.setDefaultValue(H2);
      builder.add(dbType).add(databaseURL).add(dataSourceName);

      initProviders();
      builder.add(jpaProvider);
   }

   private void initProviders() {
      jpaProvider.setItemLabelConverter(PersistenceProvider::getName);
      jpaProvider.setDefaultValue(defaultProvider);
   }

   @Override
   public void validate(UIValidationContext validator) {
      final DatabaseType database = dbType.getValue();
      final DatabaseDriver driver = DatabaseDriver.fromProductName(database.name());
      if (driver.equals(DatabaseDriver.UNKNOWN)) {
         // Spring Boot doesn't know about this DB
         validator.addValidationError(dbType, "Spring Boot doesn't know about DB '" + dbType.getName() + "'");
      }

      if (isNotEmbeddedDB(database) && !databaseURL.hasValue() && !dataSourceName.hasValue()) {
         validator.addValidationError(dataSourceName, "Either DataSource name or database URL is required");
         validator.addValidationError(databaseURL, "Either DataSource name or database URL is required");
      }
   }

   @Override
   public Result execute(final UIExecutionContext context) throws Exception {
      final UIContext uiContext = context.getUIContext();
      applyUIValues(uiContext);

      // add driver dependency
      final Project project = helper.getProject(uiContext);
      final DatabaseType database = dbType.getValue();
      final Coordinate driverCoordinate = database.getDriverCoordinate();
      SpringBootHelper.addDependency(project, driverCoordinate.getGroupId(), driverCoordinate.getArtifactId())
            .setScopeType("runtime");
      SpringBootHelper.addSpringBootDependency(project, SpringBootFacet.SPRING_BOOT_STARTER_DATA_JPA)
            .setScopeType("runtime");

      return Results.success("Spring Boot JPA successfully set up!");
   }

   @Override
   public NavigationResult next(UINavigationContext context) throws Exception {
      applyUIValues(context.getUIContext());

      final DatabaseType database = dbType.getValue();
      if (!isNotEmbeddedDB(database)) {
         return Results.navigateTo(FinishJPASetupCommand.class);
      } else {
         if (dataSourceName.hasValue()) {
            // if we specified a datasource, use it
            return Results.navigateTo(AddJNDIDatasourceCommand.class);
         } else {
            // otherwise, we're using the DB URL, so deal with it
            return Results.navigateTo(AddDBURLCommand.class);
         }
      }
   }

   private JPADataSource applyUIValues(final UIContext context) {
      Map<Object, Object> attributeMap = context.getAttributeMap();

      final Project project = helper.getProject(context);
      helper.installJPAFacet(project);

      final SpringBootPersistenceContainer container = new SpringBootPersistenceContainer();
      attributeMap.put(PersistenceContainer.class, container);
      final PersistenceProvider provider = jpaProvider.getValue();
      attributeMap.put(PersistenceProvider.class, provider);
      final DatabaseType database = dbType.getValue();
      attributeMap.put(DatabaseType.class, database);
      final JPADataSource dataSource = new JPADataSource();
      dataSource.setDatabase(database);
      dataSource.setContainer(container);
      dataSource.setProvider(provider);
      dataSource.setJndiDataSource(dataSourceName.getValue());
      dataSource.setDatabaseURL(databaseURL.getValue());
      attributeMap.put(JPADataSource.class, dataSource);

      return dataSource;
   }

   @Override
   protected boolean isProjectRequired() {
      return true;
   }
}
