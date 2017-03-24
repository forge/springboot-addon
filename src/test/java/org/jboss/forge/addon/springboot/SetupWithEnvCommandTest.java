/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.springboot;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.maven.resources.MavenModelResource;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.springboot.commands.setup.SetupProjectCommand;
import org.jboss.forge.addon.springboot.dto.SpringBootDependencyDTO;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.jboss.forge.parser.xml.Node;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:cmoullia@redhat.com">Charles Moulliard</a>
 */
@RunWith(Arquillian.class)
public class SetupWithEnvCommandTest
{

	private ProjectFactory projectFactory;
	private UITestHarness uiTestHarness;

	private Project project;

	@Before
	public void setUp() throws Exception {
		AddonRegistry addonRegistry = Furnace.instance(getClass().getClassLoader()).getAddonRegistry();
		projectFactory = addonRegistry.getServices(ProjectFactory.class).get();
		uiTestHarness = addonRegistry.getServices(UITestHarness.class).get();
		project = projectFactory.createTempProject();
	}

	@Test
	public void checkSpringBootVersion() throws Exception {
		CommandController controller = uiTestHarness
				.createCommandController(SetupProjectCommand.class, project.getRoot());
		controller.initialize();
		// Checks the command metadata
		assertTrue(controller.getCommand() instanceof SetupProjectCommand);

		if (System.getenv("SPRING_BOOT_DEFAULT_VERSION") != null) {
			assertEquals("1.5.1", controller.getValueFor("springBootVersion"));
		}
		else {
			assertEquals("1.4.1", controller.getValueFor("springBootVersion"));
		}
	}

}
