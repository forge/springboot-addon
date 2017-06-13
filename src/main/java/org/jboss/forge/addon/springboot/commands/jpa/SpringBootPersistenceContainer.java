/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.jpa;

import static org.jboss.forge.addon.javaee.jpa.DatabaseType.*;

import org.jboss.forge.addon.javaee.jpa.DatabaseType;
import org.jboss.forge.addon.javaee.jpa.JPADataSource;
import org.jboss.forge.addon.javaee.jpa.PersistenceContainer;
import org.jboss.forge.furnace.util.Strings;
import org.jboss.shrinkwrap.descriptor.api.persistence.PersistenceUnitCommon;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class SpringBootPersistenceContainer implements PersistenceContainer
{

   @Override
   public String getName(boolean isGUI) {
      return isGUI ? "Spring Boot" : "SPRINGBOOT";
   }

   @Override
   public void validate(JPADataSource dataSource) throws Exception {
      if (isNotEmbeddedDB(dataSource.getDatabase()) && Strings.isNullOrEmpty(dataSource.getDatabaseURL())) {
         throw new RuntimeException("Must specify database URL for JDBC connections.");
      }
   }

   @Override
   public PersistenceUnitCommon setupConnection(PersistenceUnitCommon unit, JPADataSource dataSource) {
      unit.transactionType("JTA");

      if (dataSource.getDatabase() == null)
      {
         dataSource.setDatabase(DatabaseType.H2);
      }

      return unit;
   }

   static boolean isNotEmbeddedDB(DatabaseType database) {
      return !database.equals(H2) && !database.equals(DERBY) && !database.equals(HSQLDB);
   }

   @Override
   public boolean isDataSourceRequired() {
      return false;
   }
}
