/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.springboot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SpringBootDependencyDTO
{

   private String group;
   private String id;
   private String name;
   private String description;

   // maven coordinates
   private String groupId;
   private String artifactId;
   private String version;

   public SpringBootDependencyDTO()
   {
   }

   public SpringBootDependencyDTO(String group, String id, String name, String description)
   {
      this.group = group;
      this.id = id;
      this.name = name;
      this.description = description;
   }

   public String getGroup()
   {
      return group;
   }

   public void setGroup(String group)
   {
      this.group = group;
   }

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public String getName()
   {
      return name;
   }

   public String getGroupAndName()
   {
      return group + ": " + name;
   }

   public String getGroupDashName()
   {
      return group + "-" + name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getGroupId()
   {
      return groupId;
   }

   public void setGroupId(String groupId)
   {
      this.groupId = groupId;
   }

   public String getArtifactId()
   {
      return artifactId;
   }

   public void setArtifactId(String artifactId)
   {
      this.artifactId = artifactId;
   }

   public String getVersion()
   {
      return version;
   }

   public void setVersion(String version)
   {
      this.version = version;
   }

   public void setMavenCoord(String groupId, String artifactId, String version)
   {
      setGroupId(groupId);
      setArtifactId(artifactId);
      setVersion(version);
   }
}
