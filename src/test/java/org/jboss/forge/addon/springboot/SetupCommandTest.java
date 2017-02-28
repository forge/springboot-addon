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

	@Before
	public void setUp() throws Exception {
		AddonRegistry addonRegistry = Furnace.instance(getClass().getClassLoader()).getAddonRegistry();
		projectFactory = addonRegistry.getServices(ProjectFactory.class).get();
		uiTestHarness = addonRegistry.getServices(UITestHarness.class).get();
		shellTest = addonRegistry.getServices(ShellTest.class).get();
		project = projectFactory.createTempProject();
/*		projectFactory = SimpleContainer.getServices(getClass().getClassLoader(), ProjectFactory.class).get();
		uiTestHarness = SimpleContainer.getServices(getClass().getClassLoader(), UITestHarness.class).get();
		shellTest = SimpleContainer.getServices(getClass().getClassLoader(), ShellTest.class).get();
		project = projectFactory.createTempProject();*/
	}

	@After
	public void tearDown() throws Exception {
		if (shellTest != null) {
			shellTest.close();
		}
	}

	@Test
	public void checkCommandMetadata() throws Exception {
		try (CommandController controller = uiTestHarness.createCommandController(SpringBootNewProjectCommand.class, project.getRoot())) {
			controller.initialize();
			// Checks the command metadata
			assertTrue(controller.getCommand() instanceof SpringBootNewProjectCommand);
			UICommandMetadata metadata = controller.getMetadata();
			assertEquals("Spring Boot: New Project", metadata.getName());
			assertEquals("Spring Boot", metadata.getCategory().getName());
		}
	}

}
