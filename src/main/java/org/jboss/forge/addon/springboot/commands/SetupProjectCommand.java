/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.springboot.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.client.Client;

import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.rest.ClientFactory;
import org.jboss.forge.addon.springboot.dto.SpringBootDependencyDTO;
import org.jboss.forge.addon.springboot.utils.CollectionStringBuffer;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.output.UIOutput;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Commands;
import org.jboss.forge.addon.ui.util.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import static org.jboss.forge.addon.maven.archetype.ArchetypeHelper.recursiveDelete;
import static org.jboss.forge.addon.springboot.utils.ConvertHelper.jsonToMap;
import static org.jboss.forge.addon.springboot.utils.ConvertHelper.removeDoubleQuotes;
import static org.jboss.forge.addon.springboot.utils.IOHelper.close;
import static org.jboss.forge.addon.springboot.utils.IOHelper.copyAndCloseInput;
import static org.jboss.forge.addon.springboot.utils.UnzipHelper.unzip;

public class SetupProjectCommand extends AbstractSpringBootCommand
{

   private static final transient Logger LOG = LoggerFactory.getLogger(SetupProjectCommand.class);

   // lets use a different category for this command
   private static final String CATEGORY = "Spring Boot";

   private static String SPRING_BOOT_CONFIG_FILE;
   private static String SPRING_BOOT_DEFAULT_VERSION;
   private static String[] SPRING_BOOT_VERSIONS;

   private static final String STARTER_ZIP_URL = "https://start.spring.io/starter.zip";
   private static final String STARTER_URL = "https://start.spring.io";
   private static List<Map> deps = new ArrayList<Map>();

   private UIOutput uiOutput;

   public SetupProjectCommand()
   {
      SPRING_BOOT_DEFAULT_VERSION =
               System.getenv("SPRING_BOOT_DEFAULT_VERSION") != null ?
                        System.getenv("SPRING_BOOT_DEFAULT_VERSION") :
                        "1.4.1";
      SPRING_BOOT_VERSIONS = System.getenv("SPRING_BOOT_VERSIONS") != null ?
               splitVersions(System.getenv("SPRING_BOOT_VERSIONS")) :
               new String[] { "1.3.8", "1.4.1", "1.4.3" };

      SPRING_BOOT_CONFIG_FILE = System.getenv("SPRING_BOOT_CONFIG_FILE");
   }

   @Inject
   @WithAttributes(label = "Spring Boot Version", description = "Spring Boot Version to use")
   private UISelectOne<String> springBootVersion;

   @Inject
   @WithAttributes(label = "Dependencies", description = "Add Spring Boot Starters and dependencies to your application")
   private UISelectMany<SpringBootDependencyDTO> dependencies;

   @Inject
   private ClientFactory factory;

   private List<SpringBootDependencyDTO> choices;

   public static String[] splitVersions(String s)
   {
      return s.split(",");
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
      uiOutput = builder.getUIContext().getProvider().getOutput();
      springBootVersion.setValueChoices(Arrays.asList(SPRING_BOOT_VERSIONS));
      springBootVersion.setDefaultValue(SPRING_BOOT_DEFAULT_VERSION);

      try
      {
         choices = initDependencies();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new IllegalStateException(
                  "Error loading dependencies from spring-boot-application.yaml due: "
                           + e.getMessage(), e);
      }

      dependencies.setValueChoices(choices);
      if (builder.getUIContext().getProvider().isGUI())
      {
         dependencies.setItemLabelConverter(SpringBootDependencyDTO::getGroupAndName);
      }
      else
      {
         // if in CLI mode then use shorter names so they are tab friendly in the shell
         dependencies.setItemLabelConverter(
                  dto -> Commands.shellifyCommandName(dto.getName()));
      }

      dependencies.setValueConverter(s ->
      {
         for (SpringBootDependencyDTO dto : choices)
         {
            if (dto.getId().equals(s))
            {
               return dto;
            }
         }
         return null;
      });

      builder.add(springBootVersion).add(dependencies);
   }

   private List<SpringBootDependencyDTO> initDependencies() throws Exception
   {
      List<SpringBootDependencyDTO> list = new ArrayList<>();

      for (Object dep : fetchDependencies())
      {
         Map<String,Object> group = (Map) dep;
         String groupName = removeDoubleQuotes(group.get("name"));
         List<Map<String,String>> content;
         // Add this test as the json file & yaml file uses a different key
         if (group.get("content") != null)
         {
            content = (List) group.get("content");
         }
         else
         {
            content = (List) group.get("values");
         }
         for (Object row : content)
         {
            Map<String, Object> item = (Map) row;
            String id = removeDoubleQuotes(item.get("id"));
            String name = removeDoubleQuotes(item.get("name"));
            String description = removeDoubleQuotes(item.get("description"));
            list.add(new SpringBootDependencyDTO(groupName, id, name, description));
         }
      }
      return list;
   }

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass())
               .category(Categories.create(CATEGORY)).name(CATEGORY + ": Setup")
               .description("Create a new Spring Boot project");
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      UIContext uiContext = context.getUIContext();
      Project project = (Project) uiContext.getAttributeMap().get(Project.class);
      if (project == null)
      {
         project = getSelectedProject(context.getUIContext());
      }

      MetadataFacet metadataFacet = project.getFacet(MetadataFacet.class);
      String projectName = metadataFacet.getProjectName();
      String projectGroupId = metadataFacet.getProjectGroupName();
      String projectVersion = metadataFacet.getProjectVersion();
      File folder = project.getRoot().reify(DirectoryResource.class).getUnderlyingResourceObject();

      Map<String, SpringBootDependencyDTO> selectedDTOs = new HashMap<>();
      int[] selected = dependencies.getSelectedIndexes();
      CollectionStringBuffer csbSpringBoot = new CollectionStringBuffer(",");
      for (int val : selected)
      {
         SpringBootDependencyDTO dto = choices.get(val);
         csbSpringBoot.append(dto.getId());
         selectedDTOs.put(dto.getId(), dto);
      }
      String springBootDeps = csbSpringBoot.toString();

      // boot version need the RELEASE suffix
      String bootVersion = springBootVersion.getValue() + ".RELEASE";

      String url = String
               .format("%s?bootVersion=%s&groupId=%s&artifactId=%s&version=%s&packageName=%s&dependencies=%s",
                        STARTER_ZIP_URL, bootVersion, projectGroupId, projectName, projectVersion, projectGroupId,
                        springBootDeps);

      LOG.info("About to query url: " + url);
      uiOutput.info(uiOutput.out(), "About to query spring starter: " + url);

      Client client = factory.createClient();
      InputStream is = client.target(url)
                              .request()
                              .get(InputStream.class);

      // some archetypes might not use maven or use the maven source layout so lets remove
      // the pom.xml and src folder if its already been pre-created
      // as these will be created if necessary via the archetype jar's contents
      File pom = new File(folder, "pom.xml");
      if (pom.isFile() && pom.exists())
      {
         pom.delete();
      }
      File src = new File(folder, "src");
      if (src.isDirectory() && src.exists())
      {
         recursiveDelete(src);
      }

      File name = new File(folder, projectName + ".zip");
      if (name.exists())
      {
         name.delete();
      }

      FileOutputStream fos = new FileOutputStream(name, false);
      copyAndCloseInput(is, fos);
      close(fos);

      // unzip the download from spring starter
      unzip(name, folder);

      LOG.info("Unzipped file to folder: {}", folder.getAbsolutePath());
      uiOutput.info(uiOutput.out(),
               "Unzipped file to folder: " + folder.getAbsolutePath());

      // and delete the zip file
      name.delete();

      // are there any fabric8 dependencies to add afterwards?
      return Results.success(
               "Created new Spring Boot project in directory: " + folder.getName());
   }

   private List fetchDependencies() throws Exception
   {
      if(deps.size() > 0) {
         return deps;
      } else {
         // Check if we have a Spring Boot Config File
         if (SPRING_BOOT_CONFIG_FILE != null)
         {
            uiOutput.info(uiOutput.out(),"Use spring boot yaml config file");
                     Yaml yaml = new Yaml();
            InputStream input = new URL(SPRING_BOOT_CONFIG_FILE).openStream();
            Map<String, Map> data = (Map) yaml.load(input);
            Map<String,List<Map>> initializer = (Map) data.get("initializr");
            deps = (List) initializer.get("dependencies");
         }
         else
         {
            // Fetch the dependencies list from the start.spring.io server
            uiOutput.info(uiOutput.out(),"Fetch deps from start.spring.io");
            Client client = factory.createClient();
            String response = client.target(STARTER_URL)
                     .request()
                     .get(String.class);

            Map<String,Object> data = jsonToMap(response);
            Map<String,List<Map>> dependencies = (Map) data.get("dependencies");
            deps = (List) dependencies.get("values");
         }
         return deps;
      }
   }
}
