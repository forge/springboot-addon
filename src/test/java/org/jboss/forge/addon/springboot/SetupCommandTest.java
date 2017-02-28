package org.jboss.forge.addon.springboot;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.shell.test.ShellTest;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.AddonRegistry;
import org.junit.After;
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
	private ShellTest shellTest;

	private Project project;
	private String springBootVersion;

	@Before
	public void setUp() throws Exception {
		AddonRegistry addonRegistry = Furnace.instance(getClass().getClassLoader()).getAddonRegistry();
		projectFactory = addonRegistry.getServices(ProjectFactory.class).get();
		uiTestHarness = addonRegistry.getServices(UITestHarness.class).get();
		shellTest = addonRegistry.getServices(ShellTest.class).get();
		project = projectFactory.createTempProject();
	}

	@After
	public void tearDown() throws Exception {
		if (shellTest != null) {
			shellTest.close();
		}
	}

	@Test
	public void checkCommandMetadata() throws Exception {
		CommandController controller = uiTestHarness
				.createCommandController(SpringBootNewProjectCommand.class, project.getRoot());
		controller.initialize();
		// Checks the command metadata
		assertTrue(controller.getCommand() instanceof SpringBootNewProjectCommand);
		SpringBootNewProjectCommand springBootCommand = (SpringBootNewProjectCommand) controller.getCommand();
		UICommandMetadata metadata = controller.getMetadata();
		assertEquals("Spring Boot: New Project", metadata.getName());
		assertEquals("Spring Boot", metadata.getCategory().getName());
	}

	@Test
	public void checkSpringBootVersion() throws Exception {
		CommandController controller = uiTestHarness
				.createCommandController(SpringBootNewProjectCommand.class, project.getRoot());
		controller.initialize();
		// Checks the command metadata
		assertTrue(controller.getCommand() instanceof SpringBootNewProjectCommand);
		SpringBootNewProjectCommand springBootCommand = (SpringBootNewProjectCommand) controller
				.getCommand();

		if (System.getProperty("spring.boot.default.version") != null) {
			assertEquals("1.5.1", springBootCommand.getSpringBootDefaultVersion());
		}
		else {
			assertEquals("1.4.1", springBootCommand.getSpringBootDefaultVersion());
		}
	}

	@Test
	public void checkSpringBootVersions() throws Exception {
		CommandController controller = uiTestHarness
				.createCommandController(SpringBootNewProjectCommand.class, project.getRoot());
		controller.initialize();
		// Checks the command metadata
		assertTrue(controller.getCommand() instanceof SpringBootNewProjectCommand);
		SpringBootNewProjectCommand springBootCommand = (SpringBootNewProjectCommand) controller.getCommand();

		String[] versions = springBootCommand.getSpringBootVersions();
		if (System.getProperty("spring.boot.versions") != null) {
			assertEquals("1.4.3",versions[2]);
		} else {
			assertEquals("1.4.1",versions[1]);
		}
	}

}
