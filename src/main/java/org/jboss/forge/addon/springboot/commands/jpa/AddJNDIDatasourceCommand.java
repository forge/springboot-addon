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
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.WithAttributes;

import javax.inject.Inject;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class AddJNDIDatasourceCommand extends AbstractDataSourceCommand {
   @Inject
   @WithAttributes(shortName = 'd', label = "DataSource Name", required = true)
   private UIInput<String> dataSourceName;

   @Override
   public void initializeUI(UIBuilder builder) throws Exception {
      builder.add(dataSourceName);
   }

   @Override
   protected void updateDataSource(JPADataSource dataSource) {
      dataSource.setJndiDataSource(dataSourceName.getValue());
   }

   @Override
   protected void setProperties(CollectionStringBuffer buffer) {
      buffer.append("spring.datasource.jndi-name=" + dataSourceName.getValue());
   }
}
