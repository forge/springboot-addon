/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.rest;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.forge.addon.javaee.rest.generation.RestGenerationContext;
import org.jboss.forge.addon.javaee.rest.generation.RestResourceGenerator;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.templates.Template;
import org.jboss.forge.addon.templates.TemplateFactory;
import org.jboss.forge.addon.templates.freemarker.FreemarkerTemplate;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.util.Types;

/**
 * A Spring Boot specific {@link org.jboss.forge.addon.javaee.rest.generation.RestResourceGenerator} that overrides the
 * default code generation template, which needs to be put in the resources of this addon under the {@code Endpoint.jv}
 * name.
 *
 * @author <a href="claprun@redhat.com">Christophe Laprun</a>
 */
public class SpringRestResourceGenerator implements RestResourceGenerator
{
   @Inject
   TemplateFactory templateFactory;

   @Inject
   ResourceFactory resourceFactory;

   @Override
   public List<JavaClassSource> generateFrom(RestGenerationContext context) throws Exception
   {
      JavaClassSource entity = context.getEntity();
      Project project = context.getProject();
      if (!entity.hasAnnotation(XmlRootElement.class))
      {
         entity.addAnnotation(XmlRootElement.class);
         project.getFacet(JavaSourceFacet.class).saveJavaSource(entity);
      }

      String idType = JPAEntityUtil.resolveIdType(entity);
      String idGetterName = JPAEntityUtil.resolveIdGetterName(entity);
      String entityTable = JPAEntityUtil.getEntityTable(entity);
      String selectExpression = JPAEntityUtil.getSelectExpression(entity, entityTable);
      String idClause = JPAEntityUtil.getIdClause(entity, entityTable);
      String orderClause = JPAEntityUtil.getOrderClause(entity,
               JPAEntityUtil.getJpqlEntityVariable(entityTable));
      String resourcePath = getResourcePath(context);

      Map<Object, Object> map = new HashMap<>();
      map.put("entity", entity);
      map.put("idType", idType);
      map.put("getIdStatement", idGetterName);
      map.put("contentType", "\"application/json\""); // fixme: hardcoded JSON representation
      map.put("entityTable", entityTable);
      map.put("selectExpression", selectExpression);
      map.put("idClause", idClause);
      map.put("orderClause", orderClause);
      map.put("resourcePath", resourcePath);
      map.put("idIsPrimitive", Types.isPrimitive(idType));

      Resource<URL> templateResource = resourceFactory.create(getClass().getResource("Endpoint.jv"));
      Template processor = templateFactory.create(templateResource, FreemarkerTemplate.class);
      String output = processor.process(map);
      JavaClassSource resource = Roaster.parse(JavaClassSource.class, output);
      resource.addImport(entity.getQualifiedName());
      resource.setPackage(context.getTargetPackageName());
      return Arrays.asList(resource);
   }

   @Override
   public String getName()
   {
      return "SPRING_BOOT_JPA_ENTITY";
   }

   @Override
   public String getDescription()
   {
      return "Expose JPA entities directly in the REST resources using Spring Boot and CXF";
   }

   static String getResourcePath(RestGenerationContext context)
   {
      String entityTable = JPAEntityUtil.getEntityTable(context.getEntity());

      return "/" + context.getInflector().pluralize(entityTable.toLowerCase());
   }
}
