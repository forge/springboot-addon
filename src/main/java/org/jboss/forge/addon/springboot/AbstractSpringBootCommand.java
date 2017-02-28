/**
 *  Copyright 2005-2015 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package org.jboss.forge.addon.springboot;

import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;

/**
 * An abstract base class for SpringBoot related commands
 */
public abstract class AbstractSpringBootCommand extends AbstractProjectCommand {
    @Override
    public UICommandMetadata getMetadata(UIContext context)
    {
        return Metadata.forCommand(getClass()).category(Categories.create("Spring-Boot"));
    }

    @Override
    protected ProjectFactory getProjectFactory()
    {
        return SimpleContainer.getServices(getClass().getClassLoader(), ProjectFactory.class).get();
    }

    @Override
    protected boolean isProjectRequired()
    {
        return true;
    }

}
