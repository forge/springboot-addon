/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.jpa;

import org.jboss.forge.addon.javaee.jpa.JPADataSource;
import org.jboss.forge.addon.springboot.utils.CollectionStringBuffer;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class AddJNDIDatasourceCommand extends AbstractDataSourceCommand {
   @Override
   protected void updateDataSource(JPADataSource dataSource) {
   }

   @Override
   protected void setProperties(CollectionStringBuffer buffer, JPADataSource dataSource) {
      buffer.append("spring.datasource.jndi-name=" + dataSource.getJndiDataSource());
   }
}
