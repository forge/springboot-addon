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
import org.jboss.forge.addon.javaee.jpa.DatabaseType;
import org.jboss.forge.addon.javaee.jpa.JPAFacet;
import org.jboss.forge.addon.javaee.jpa.PersistenceContainer;
import org.jboss.forge.addon.javaee.jpa.PersistenceProvider;
import org.jboss.forge.addon.javaee.jpa.providers.HibernateProvider;
import org.jboss.forge.addon.javaee.jpa.ui.setup.JPASetupWizard;
import org.jboss.forge.addon.javaee.ui.AbstractJavaEECommand;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.stacks.annotations.StackConstraint;
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

import static org.jboss.forge.addon.javaee.jpa.DatabaseType.*;

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
   @WithAttributes(label = "Use JNDI datasource?", required = true, defaultValue = "false")
   private UIInput<Boolean> useJNDI;
   
   /*@Inject
   @WithAttributes(shortName = 'c', label = "Container", required = true)
   private UISelectOne<PersistenceContainer> jpaContainer;*/

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
//      UIContext uiContext = builder.getUIContext();
//      Project project = getSelectedProject(builder);

      dbType.setDefaultValue(H2);
      builder.add(dbType).add(useJNDI);

//      initContainers(project, uiContext);
      initProviders();
      builder.add(jpaProvider);
   }

   /*private void initContainers(Project project, UIContext context) {
      final boolean isGUI = context.getProvider().isGUI();
      jpaContainer.setItemLabelConverter((source) -> source.getName(isGUI));
      // Ordering items
      TreeSet<PersistenceContainer> treeSet = new TreeSet<>(
            (o1, o2) -> String.valueOf(o1.getName(isGUI)).compareTo(o2.getName(isGUI)));
      Optional<Stack> stack = project.getStack();
      for (PersistenceContainer persistenceContainer : jpaContainer.getValueChoices()) {
         if (!stack.isPresent() || persistenceContainer.supports(stack.get()))
            treeSet.add(persistenceContainer);
      }
      jpaContainer.setValueChoices(treeSet);
      if (treeSet.contains(defaultContainer)) {
         jpaContainer.setDefaultValue(defaultContainer);
      }
   }*/

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
   }

   @Override
   public Result execute(final UIExecutionContext context) throws Exception {
      applyUIValues(context.getUIContext());

      final DatabaseType database = dbType.getValue();
      final Coordinate driverCoordinate = database.getDriverCoordinate();

      final Project project = helper.getProject(context.getUIContext());

      // add driver dependency
      SpringBootHelper.addDependency(project, driverCoordinate.getGroupId(), driverCoordinate.getArtifactId())
            .setScopeType("runtime");


      return Results.success("Spring Boot JPA successfully set up!");
   }

/*   private PersistenceProvider wrapPersistenceProvider(Map<Object, Object> attributeMap) {
      final PersistenceProvider provider = jpaProvider.getValue();
      final PersistenceProvider wrapped = new PersistenceProvider() {
         @Override
         public String getName() {
            return provider.getName();
         }

         @Override
         public String getProvider() {
            return provider.getProvider();
         }

         @Override
         public PersistenceUnitCommon configure(PersistenceUnitCommon unit, JPADataSource ds, Project project) {
            return provider.configure(unit, ds, project);
         }

         @Override
         public List<Dependency> listDependencies() {
            return provider.listDependencies();
         }

         @Override
         public MetaModelProvider getMetaModelProvider() {
            return provider.getMetaModelProvider();
         }

         @Override
         public void validate(JPADataSource dataSource) throws Exception {
            // put data source in attribute map so that it can be used in other commands
            attributeMap.put(JPADataSource.class, dataSource);

            provider.validate(dataSource);
         }
      };

      // replace original provider by wrapped version
      attributeMap.put(PersistenceProvider.class, wrapped);

      return wrapped;
   }*/

   @Override
   public NavigationResult next(UINavigationContext context) throws Exception {
      applyUIValues(context.getUIContext());

      final DatabaseType database = dbType.getValue();
      if (database.equals(H2) || database.equals(DERBY) || database.equals(HSQLDB)) {
         // if we're using H2, Derby or HSQL embedded databases, we're done!
         return null;
      } else {
         // redirect to appropriate next step depending on whether we want to use a JNDI data source or not
         if (useJNDI.getValue()) {
            return Results.navigateTo(AddJNDIDatasourceCommand.class);
         } else {
            return Results.navigateTo(AddDBURLCommand.class);
         }
      }
   }

   private void applyUIValues(final UIContext context) {
      Map<Object, Object> attributeMap = context.getAttributeMap();

      final Project project = helper.getProject(context);
      final SpringBootJPAFacet jpaFacet = helper.installJPAFacet(project);

      attributeMap.put(JPAFacet.class, jpaFacet);
      attributeMap.put(PersistenceContainer.class, new SpringBootPersistenceContainer());
      attributeMap.put(DatabaseType.class, dbType.getValue());
//      wrapPersistenceProvider(attributeMap);
   }

   @Override
   protected boolean isProjectRequired() {
      return true;
   }
}
