/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.jpa;

import org.jboss.forge.addon.javaee.jpa.JPADataSource;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.WithAttributes;

import javax.inject.Inject;
import java.util.Properties;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class AddDBURLCommand extends AbstractDataSourceCommand {
   @Inject
   @WithAttributes(label = "Username", required = true)
   private UIInput<String> username;

   @Inject
   @WithAttributes(label = "Password", type = InputType.SECRET, required = true)
   private UIInput<String> password;

   @Override
   public void initializeUI(UIBuilder builder) throws Exception {
      builder.add(username).add(password);
   }

   @Override
   protected void updateDataSource(JPADataSource dataSource) {
      dataSource.setUsername(username.getValue()).setPassword(password.getValue());
   }

   @Override
   protected void setProperties(Properties properties, JPADataSource dataSource) {
      properties.put(SpringBootJPAFacet.SPRING_DATASOURCE_PROPERTIES_PREFIX + "url", dataSource.getDatabaseURL());
      properties.put(SpringBootJPAFacet.SPRING_DATASOURCE_PROPERTIES_PREFIX + "username", username.getValue());
      properties.put(SpringBootJPAFacet.SPRING_DATASOURCE_PROPERTIES_PREFIX + "password", password.getValue());
   }
}
