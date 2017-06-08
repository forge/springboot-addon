/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.rest;

import org.jboss.forge.addon.javaee.rest.generator.impl.EntityBasedResourceGenerator;

/**
 * A Spring Boot specific {@link org.jboss.forge.addon.javaee.rest.generation.RestResourceGenerator} that overrides
 * the default code generation template, which needs to be put in the resources of this addon under the
 * {@code Endpoint.jv} name (this name MUST match the template that {@link EntityBasedResourceGenerator} looks for).
 *
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class SpringRestResourceGenerator extends EntityBasedResourceGenerator {
   @Override
   public String getName() {
      return "SPRING_BOOT_JPA_ENTITY";
   }

   @Override
   public String getDescription() {
      return "Expose JPA entities directly in the REST resources using Spring Boot and CXF";
   }
}
