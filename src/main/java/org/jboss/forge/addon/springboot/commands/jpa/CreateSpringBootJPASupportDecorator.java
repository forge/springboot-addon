/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.jpa;

import javax.persistence.Id;

import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.ui.AbstractJavaSourceCommand;
import org.jboss.forge.addon.parser.java.ui.JavaSourceDecorator;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;

/**
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class CreateSpringBootJPASupportDecorator implements JavaSourceDecorator<JavaClassSource> {
   private final AbstractJavaSourceCommand original;

   public CreateSpringBootJPASupportDecorator(AbstractJavaSourceCommand original) {
      this.original = original;
   }

   @Override
   public JavaClassSource decorateSource(UIExecutionContext context, Project project, JavaClassSource source) throws Exception {
      // call the original command
      final JavaClassSource newSource = (JavaClassSource) original.decorateSource(context, project, source);

      createCRUDRepository(newSource, project);

      // return the output of the wrapped command
      return newSource;
   }

   private void createCRUDRepository(JavaClassSource entitySource, Project project) {
      // build interface declaration
      final String name = entitySource.getName();
      final String idFieldType = entitySource.getFields().stream()
            .filter(field -> field.hasAnnotation(Id.class))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new)
            .getType()
            .getSimpleName();
      final String repoInterfaceDeclaration = "public interface " + name + "Repository extends CrudRepository<"
            + name + "," + idFieldType + "> {}";

      JavaInterfaceSource repoSource = Roaster.parse(JavaInterfaceSource.class, repoInterfaceDeclaration)
            .setPackage(entitySource.getPackage());
      repoSource.addImport("org.springframework.data.repository.CrudRepository");
      Roaster.format(repoSource.toString());

      // Create Java Classes Greeting and GreetingProperties
      JavaSourceFacet facet = project.getFacet(JavaSourceFacet.class);
      facet.saveJavaSource(repoSource);
   }
}
