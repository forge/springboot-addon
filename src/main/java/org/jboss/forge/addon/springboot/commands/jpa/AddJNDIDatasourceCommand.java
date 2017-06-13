/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.jpa;

import org.jboss.forge.addon.javaee.jpa.JPADataSource;

import java.util.Properties;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class AddJNDIDatasourceCommand extends AbstractDataSourceCommand {
   @Override
   protected void updateDataSource(JPADataSource dataSource) {
   }

   @Override
   protected void setProperties(Properties properties, JPADataSource dataSource) {
      properties.put(SpringBootJPAFacet.SPRING_DATASOURCE_PROPERTIES_PREFIX + "jndi-name",
            dataSource.getJndiDataSource());
   }
}
