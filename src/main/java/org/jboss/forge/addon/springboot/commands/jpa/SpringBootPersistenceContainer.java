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
import org.jboss.forge.addon.javaee.jpa.containers.JavaEEDefaultContainer;
import org.jboss.forge.furnace.util.Strings;
import org.jboss.shrinkwrap.descriptor.api.persistence.PersistenceUnitCommon;

import static org.jboss.forge.addon.javaee.jpa.DatabaseType.*;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class SpringBootPersistenceContainer extends JavaEEDefaultContainer {

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
   public String getDefaultDataSource() {
      return null;
   }

   @Override
   public DatabaseType getDefaultDatabaseType() {
      return DatabaseType.H2;
   }

   @Override
   public PersistenceUnitCommon setupConnection(PersistenceUnitCommon unit, JPADataSource dataSource) {
      final DatabaseType database = dataSource.getDatabase();
      final Coordinate driverCoordinate = database.getDriverCoordinate();

      //todo

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
