/**
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.aggregate;

import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.ui.controller.WizardCommandController;
import org.jboss.forge.addon.ui.result.CompositeResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.AddonDeployment;
import org.jboss.forge.arquillian.AddonDeployments;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;

/**
 * 
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
@RunWith(Arquillian.class)
public class WizardCommandControllerTest
{

   @Deployment
   @AddonDependencies({
           @AddonDependency(name = "org.jboss.forge.addon:ui-test-harness"),
           @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
   })
   public static AddonArchive getDeployment()
   {
      return ShrinkWrap.create(AddonArchive.class).addBeansXML().addClasses(AggregateWizard.class, ExampleCommand.class, ExampleTwoCommand.class);
   }

   @Inject
   UITestHarness testHarness;

   @Test
   public void testAggregateWizard() throws Exception
   {
      try (WizardCommandController controller = testHarness.createWizardController(AggregateWizard.class))
      {
         controller.initialize();
         Assert.assertFalse(controller.canMoveToNextStep());
         controller.setValueFor("value", "Anything");
         Assert.assertTrue(controller.canMoveToNextStep());
         controller.next().initialize();
         controller.setValueFor("firstName", "George");
         controller.setValueFor("lastName", "Gastaldi");
         Assert.assertFalse(controller.canMoveToNextStep());
         Assert.assertTrue(controller.canExecute());
         Result result = controller.execute();
         Assert.assertThat(result, instanceOf(CompositeResult.class));
         CompositeResult compositeResult = (CompositeResult) result;
         List<Result> results = compositeResult.getResults();
         Assert.assertEquals(2, results.size());
         Assert.assertThat(results.get(0), not(instanceOf(CompositeResult.class)));
         Assert.assertEquals("Anything", results.get(0).getMessage());
         Assert.assertThat(results.get(1), instanceOf(CompositeResult.class));
         CompositeResult nestedResult = (CompositeResult) results.get(1);
         Assert.assertEquals("Hello, George", nestedResult.getResults().get(0).getMessage());
         Assert.assertEquals("Goodbye, Gastaldi", nestedResult.getResults().get(1).getMessage());
      }
   }
}
