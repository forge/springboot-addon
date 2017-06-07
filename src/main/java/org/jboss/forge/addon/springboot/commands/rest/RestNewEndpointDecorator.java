/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.jboss.forge.addon.springboot.commands.rest;

import org.jboss.forge.addon.javaee.rest.ui.RestMethod;
import org.jboss.forge.addon.javaee.rest.ui.RestNewEndpointCommand;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.ui.JavaSourceDecorator;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.springboot.SpringBootFacet;
import org.jboss.forge.addon.springboot.utils.SpringBootHelper;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.UriBuilder;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link JavaSourceDecorator} that generates Spring-specific code when call from the {@code rest-new-endpoint}
 * command.
 *
 * @author <a href="claprun@redhat.com>Christophe Laprun</a>
 */
public class RestNewEndpointDecorator implements JavaSourceDecorator<JavaClassSource> {
   private final RestNewEndpointCommand wrapped;

   public RestNewEndpointDecorator(RestNewEndpointCommand wrapped) {
      this.wrapped = wrapped;
   }

   public static JavaClassSource createGreetingPropertiesClass(JavaClassSource current) {
      JavaClassSource source = Roaster.create(JavaClassSource.class).setName("GreetingProperties").setPackage(current.getPackage());
      source.addAnnotation(Component.class);
      source.addAnnotation(ConfigurationProperties.class).setStringValue("greeting");
      source.addProperty(String.class, "message").getField().setStringInitializer("Hello, %s!");
      return source;
   }

   public static JavaClassSource createGreetingClass(JavaClassSource current) {
      JavaClassSource source = Roaster.create(JavaClassSource.class).setName("Greeting").setPackage(current.getPackage());
      source.addMethod().setPublic().setConstructor(true).setBody("this.id = 0;this.content = null;");
      source.addMethod().setPublic().setConstructor(true).setParameters("long id, String content").setBody("this.id = id; this.content = content;");
      source.addProperty(String.class, "content");
      source.addProperty("long", "id");
      Roaster.format(source.toString());
      return source;
   }

   @Override
   public JavaClassSource decorateSource(UIExecutionContext context, Project project, JavaClassSource source) throws Exception {
      // Check that we have the spring-boot-starter-web dependency and add it if we don't
      SpringBootHelper.addSpringBootDependency(project, SpringBootFacet.SPRING_BOOT_STARTER_WEB);

      // Create Java Classes Greeting and GreetingProperties
      JavaSourceFacet facet = project.getFacet(JavaSourceFacet.class);
      facet.saveJavaSource(createGreetingClass(source));
      facet.saveJavaSource(createGreetingPropertiesClass(source));

      final Properties properties = new Properties();
      if (wrapped.getPath().hasValue()) {
         final String value = wrapped.getPath().getValue();
         properties.put("server.contextPath", (value.startsWith("/") ? value : "/" + value));
      }
      properties.put("greeting.message", "Hello, %s!");
      SpringBootHelper.writeToApplicationProperties(project, properties);

      // Create the Controller
      source.addImport(AtomicLong.class);
      source.addAnnotation(RestController.class);
      source.addField().setPrivate().setFinal(false).setType("GreetingProperties").setName("properties").addAnnotation(Autowired.class);
      source.addField().setPrivate().setFinal(true).setType("AtomicLong").setName("counter").setLiteralInitializer("new AtomicLong()");

      for (RestMethod restMethod : wrapped.getMethods().getValue()) {
         MethodSource<?> greeting = source.addMethod()
               .setPublic()
               .setName(restMethod.getMethodName())
               .setReturnType("Greeting");

         switch (restMethod) {
            case GET:
               greeting.addAnnotation(RequestMapping.class).setStringValue("/greeting");
               greeting.addParameter(String.class, "name").addAnnotation(RequestParam.class).setLiteralValue("value", "\"name\"").setLiteralValue("defaultValue", "\"world\"");
               greeting.setBody("return new Greeting(this.counter.incrementAndGet(), String.format(this.properties.getMessage(), name));");
               break;
            case POST:
               source.addImport(UriBuilder.class);
               // TODO
               break;
            case PUT:
               // TODO
               break;
            case DELETE:
               // TODO
               break;
         }
      }

      return source;

   }

}
