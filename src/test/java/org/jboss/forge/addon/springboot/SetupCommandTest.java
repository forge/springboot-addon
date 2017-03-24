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
public class SetupCommandTest {

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
	public void checkCommandMetadata() throws Exception {
		CommandController controller = uiTestHarness
				.createCommandController(SetupProjectCommand.class, project.getRoot());
		controller.initialize();
		// Checks the command metadata
		assertTrue(controller.getCommand() instanceof SetupProjectCommand);
		SetupProjectCommand springBootCommand = (SetupProjectCommand) controller.getCommand();
		UICommandMetadata metadata = controller.getMetadata();
		assertEquals("Spring Boot: Setup", metadata.getName());
		assertEquals("Spring Boot", metadata.getCategory().getName());
		Result result = controller.execute();
		assertTrue("Created new Spring Boot", result.getMessage().contains("Created new Spring Boot"));
	}

	@Test
	public void checkParametersPassed() throws Exception {
		CommandController controller = uiTestHarness
					.createCommandController(SetupProjectCommand.class, project.getRoot());
		controller.initialize();
		// Checks the command metadata
		assertTrue(controller.getCommand() instanceof SetupProjectCommand);

		SpringBootDependencyDTO securityDTO = new SpringBootDependencyDTO("Core","security","Security","Secure your application via spring-security");
		SpringBootDependencyDTO actuatorDTO = new SpringBootDependencyDTO("Ops","actuator","Actuator","Production ready features to help you monitor and manage your application");
		Iterable<SpringBootDependencyDTO> deps = Arrays.asList(securityDTO, actuatorDTO);
		controller.setValueFor("dependencies", deps);
		controller.setValueFor("springBootVersion","1.3.8");
		Result result = controller.execute();
		MavenFacet mavenFacet = project.getFacet(MavenFacet.class);
		MavenModelResource modelResource = mavenFacet.getModelResource();
		Model model = modelResource.getCurrentModel();
		assertEquals("empty-project",model.getArtifactId());
		assertEquals("unknown",model.getGroupId());
		assertEquals("0",model.getVersion());

		// Check if the parent contains the Spring Boot Artifact & version specified
		Parent parent = model.getParent();
		assertEquals("org.springframework.boot",parent.getGroupId());
		assertEquals("spring-boot-starter-parent",parent.getArtifactId());
		assertEquals("1.3.8.RELEASE",parent.getVersion());

		Node contents = modelResource.getXmlSource();
		List<Node> dependenciesNodes = contents.get("dependencies").get(0).getChildren();

		// Check Spring Boot Actuator Dependency
		List<Node> dependencyActuator = dependenciesNodes.get(0).getChildren();
		assertEquals("org.springframework.boot",dependencyActuator.get(0).getText());
		assertEquals("spring-boot-starter-actuator",dependencyActuator.get(1).getText());

		// Check Spring Boot Security Dependency
		List<Node> dependencySecurity = dependenciesNodes.get(1).getChildren();
		assertEquals("org.springframework.boot",dependencySecurity.get(0).getText());
		assertEquals("spring-boot-starter-security",dependencySecurity.get(1).getText());

		assertTrue("Created new Spring Boot", result.getMessage().contains("Created new Spring Boot"));
	}

}
