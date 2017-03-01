/**
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.addon.springboot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.springboot.dto.SpringBootDependencyDTO;
import org.jboss.forge.addon.ui.cdi.CommandScoped;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.result.navigation.NavigationResultBuilder;
import org.jboss.forge.addon.ui.util.Commands;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizard;

/**
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
public class AggregateSpringBootDevOpsWizard extends AbstractProjectCommand implements UIWizard {

	@Inject
	private ProjectFactory projectFactory;

	@Override
	protected boolean isProjectRequired() {
		return false;
	}

	@Override
	public ProjectFactory getProjectFactory() {
		return projectFactory;
	}

	@Inject
	@WithAttributes(label = "Value", required = true)
	private UIInput<String> value;

	@Override
	public void initializeUI(UIBuilder builder) throws Exception {
		builder.add(value);
	}

	@Override
	public Result execute(UIExecutionContext context) throws Exception {
		return Results.success(value.getValue());
	}

	@Override public UICommandMetadata getMetadata(UIContext context) {
		return Metadata
				  .from(super.getMetadata(context), getClass())
				  .name("aggregate-wizard");
	}

	@Override
	public NavigationResult next(UINavigationContext context) throws Exception {
		NavigationResultBuilder builder = NavigationResultBuilder.create();
		builder.add(getMetadata(context.getUIContext()),
				Arrays.asList(DevOpsCommand.class, SpringBootCommand.class));
		return builder.build();
	}

}
