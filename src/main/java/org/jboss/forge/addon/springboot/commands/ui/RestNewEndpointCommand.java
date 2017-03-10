/**
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.springboot.commands.ui;

import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.text.Inflector;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Creates a new REST Endpoint.
 *
 * @author <a href="cmoulliard@redhat.com">Charles Moulliard</a>
 */
public class RestNewEndpointCommand extends AbstractRestNewCommand<JavaClassSource>
{
   @Inject
   @WithAttributes(label = "Methods", description = "REST methods to be defined", defaultValue = "GET")
   private UISelectMany<RestMethod> methods;

   @Inject
   @WithAttributes(label = "Path", description = "The root path of the endpoint")
   private UIInput<String> path;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass())
               .name(SPRING_REST_CAT + "New Endpoint")
               .description("Creates a new Spring REST Endpoint");
   }

   @Override
   protected String getType()
   {
      return "REST";
   }

   @Override
   protected Class<JavaClassSource> getSourceType()
   {
      return JavaClassSource.class;
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      super.initializeUI(builder);
      builder.add(methods).add(path);
   }

   @Override
   public JavaClassSource decorateSource(UIExecutionContext context, Project project, JavaClassSource source)
            throws Exception
   {

      // Create Java Classes Greeting and GreetingProperties
      JavaSourceFacet facet = project.getFacet(JavaSourceFacet.class);
      facet.saveJavaSource(createGreetingClass(source));
      facet.saveJavaSource(createGreetingPropertiesClass(source));

      FileResource<?> applicationFile = project.getFacet(ResourcesFacet.class).getResource("application.properties");
      if (!applicationFile.exists()) {
         applicationFile.createNewFile();
      }

      StringBuilder sb = new StringBuilder();

      if (path.hasValue())
      {
         // Add contextPath within the application.properties file
         sb.append("server.contextPath=/" + path.getValue());
      }
      sb.append(System.getProperty("line.separator"));
      sb.append("greeting.message=Hello, %s!");
      applicationFile.setContents(sb.toString());

      // Create the Controller
      source.addImport(AtomicLong.class);
      source.addAnnotation(RestController.class);
      source.addField().setPrivate().setFinal(false).setType("GreetingProperties").setName("properties").addAnnotation(Autowired.class);
      source.addField().setPrivate().setFinal(true).setType("AtomicLong").setName("counter").setLiteralInitializer("new AtomicLong()");

      for (RestMethod method : methods.getValue())
      {
         MethodSource<?> greeting = source.addMethod()
                                          .setPublic()
                                          .setName(method.getMethodName())
                                          .setReturnType("Greeting");

         switch (method)
         {
         case GET:
            greeting.addAnnotation(RequestMapping.class).setStringValue("/greeting");
            greeting.addParameter(String.class,"name").addAnnotation(RequestParam.class).setLiteralValue("value","\"name\"").setLiteralValue("defaultValue","\"world\"");
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

   public JavaClassSource createGreetingPropertiesClass(JavaClassSource current) {
      JavaClassSource source = Roaster.create(JavaClassSource.class).setName("GreetingProperties").setPackage(current.getPackage());
      source.addAnnotation(Component.class);
      source.addAnnotation(ConfigurationProperties.class).setStringValue("greeting");
      source.addProperty(String.class,"message").getField().setStringInitializer("Hello, %s!");
      return source;
   }

   public JavaClassSource createGreetingClass(JavaClassSource current) {
      JavaClassSource source = Roaster.create(JavaClassSource.class).setName("Greeting").setPackage(current.getPackage());
      source.addMethod().setPublic().setConstructor(true).setBody("this.id = 0;this.content = null;");
      source.addMethod().setPublic().setConstructor(true).setParameters("long id, String content").setBody("this.id = id; this.content = content;");
      source.addProperty(String.class,"content");
      source.addProperty("long","id");
      Roaster.format(source.toString());
      return source;
   }
}
