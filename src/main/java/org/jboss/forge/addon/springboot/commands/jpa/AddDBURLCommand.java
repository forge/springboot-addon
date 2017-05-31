/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.jpa;

import org.jboss.forge.addon.javaee.jpa.DatabaseType;
import org.jboss.forge.addon.javaee.jpa.JPADataSource;
import org.jboss.forge.addon.springboot.utils.CollectionStringBuffer;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.WithAttributes;

import javax.inject.Inject;
import java.util.Map;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class AddDBURLCommand extends AbstractDataSourceCommand {
   @Inject
   @WithAttributes(label = "Database URL", required = true)
   private UIInput<String> databaseURL;

   @Inject
   @WithAttributes(label = "Username", required = true)
   private UIInput<String> username;

   @Inject
   @WithAttributes(label = "Password", type = InputType.SECRET, required = true)
   private UIInput<String> password;

   @Override
   public void initializeUI(UIBuilder builder) throws Exception {
      Map<Object, Object> attributeMap = builder.getUIContext().getAttributeMap();
      final DatabaseType database = (DatabaseType) attributeMap.get(DatabaseType.class);
      databaseURL.setDefaultValue("jdbc:" + database.name() + ":myDB");
      builder.add(databaseURL).add(username).add(password);
   }

   @Override
   protected void updateDataSource(JPADataSource dataSource) {
      dataSource.setDatabaseURL(databaseURL.getValue()).setUsername(username.getValue()).setPassword(password.getValue());
   }

   protected void setProperties(CollectionStringBuffer buffer) {
      buffer.append("spring.datasource.url=" + databaseURL.getValue());
      buffer.append("spring.datasource.username=" + username.getValue());
      buffer.append("spring.datasource.password=" + password.getValue());
   }
}
