/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.jpa;

import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.javaee.jpa.JPADataSource;
import org.jboss.forge.addon.javaee.jpa.MetaModelProvider;
import org.jboss.forge.addon.javaee.jpa.PersistenceProvider;
import org.jboss.forge.addon.javaee.jpa.ui.setup.JPASetupWizard;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.NavigationResultEntry;
import org.jboss.forge.addon.ui.result.navigation.NavigationResultBuilder;
import org.jboss.forge.addon.ui.result.navigation.NavigationResultTransformer;
import org.jboss.shrinkwrap.descriptor.api.persistence.PersistenceUnitCommon;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class SpringBootJPANavigationTransformer implements NavigationResultTransformer {
   @Override
   public boolean handles(UINavigationContext context) {
      return context.getCurrentCommand() instanceof JPASetupWizard;
   }

   @Override
   public NavigationResult transform(UINavigationContext context, NavigationResult original) {
      final NavigationResultEntry[] originalNext = original.getNext();


      // make datasource available so that we can add the DB jar if needed later
      final Map<Object, Object> attributeMap = context.getUIContext().getAttributeMap();
      final PersistenceProvider provider = (PersistenceProvider) attributeMap.get(PersistenceProvider.class);

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


      return NavigationResultBuilder.create(original)
            .add(AddJPADependencyCommand.class)
            .add(() -> originalNext)
            .add(AddDBJarDependencyCommand.class)
            .build();
   }

   @Override
   public int priority() {
      return 10;
   }

}
