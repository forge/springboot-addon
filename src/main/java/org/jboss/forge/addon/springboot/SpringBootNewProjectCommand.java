/**
 * Copyright 2005-2015 Red Hat, Inc.
 * <p/>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.jboss.forge.addon.springboot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.dependencies.DependencyInstaller;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.springboot.dto.SpringBootDependencyDTO;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import static org.jboss.forge.addon.maven.archetype.ArchetypeHelper.recursiveDelete;
import static org.jboss.forge.addon.springboot.IOHelper.close;
import static org.jboss.forge.addon.springboot.IOHelper.copyAndCloseInput;
import static org.jboss.forge.addon.springboot.OkHttpClientHelper.createOkHttpClient;
import static org.jboss.forge.addon.springboot.UnzipHelper.unzip;

public class SpringBootNewProjectCommand extends AbstractSpringBootCommand {

	private static final transient Logger LOG = LoggerFactory
			.getLogger(SpringBootNewProjectCommand.class);

	// lets use a different category for this command
	private static final String CATEGORY = "Spring Boot";

	private static String SPRING_BOOT_DEFAULT_VERSION;
	private static String[] SPRING_BOOT_VERSIONS;
	private static String SPRING_BOOT_CONFIG_FILE;

	private static final String STARTER_ZIP_URL = "https://start.spring.io/starter.zip";
	private static final String STARTER_URL = "https://start.spring.io";

	private UIOutput uiOutput;

	public SpringBootNewProjectCommand() {
		SPRING_BOOT_DEFAULT_VERSION =
				System.getenv("SPRING_BOOT_DEFAULT_VERSION") != null ?
						System.getenv("SPRING_BOOT_DEFAULT_VERSION") :
						"1.4.1";
		SPRING_BOOT_VERSIONS = System.getenv("SPRING_BOOT_VERSIONS") != null ?
				splitVersions(System.getenv("SPRING_BOOT_VERSIONS")) :
				new String[] { "1.3.8", "1.4.1" };

		SPRING_BOOT_CONFIG_FILE = System.getenv("SPRING_BOOT_CONFIG_FILE");
	}

	@Inject
	@WithAttributes(label = "Spring Boot Version", required = true, description = "Spring Boot Version to use")
	private UISelectOne<String> springBootVersion;

	@Inject
	@WithAttributes(label = "Dependencies", required = true, description = "Add Spring Boot Starters and dependencies to your application")
	private UISelectMany<SpringBootDependencyDTO> dependencies;

	@Inject
	private DependencyInstaller dependencyInstaller;

	private List<SpringBootDependencyDTO> choices;

	public static String[] splitVersions(String s) {
		return s.split(",");
	}

	public String getSpringBootDefaultVersion() {
		return SPRING_BOOT_DEFAULT_VERSION;
	}

	public String[] getSpringBootVersions() {
		return SPRING_BOOT_VERSIONS;
	}

	@Override
	public void initializeUI(UIBuilder builder) throws Exception {
		uiOutput = builder.getUIContext().getProvider().getOutput();
		springBootVersion.setValueChoices(Arrays.asList(SPRING_BOOT_VERSIONS));
		springBootVersion.setDefaultValue(SPRING_BOOT_DEFAULT_VERSION);

		try {
			choices = initDependencies();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException(
					"Error loading dependencies from spring-boot-application.yaml due: "
							+ e.getMessage(), e);
		}

		dependencies.setValueChoices(choices);
		if (builder.getUIContext().getProvider().isGUI()) {
			dependencies.setItemLabelConverter(SpringBootDependencyDTO::getGroupAndName);
		}
		else {
			// if in CLI mode then use shorter names so they are tab friendly in the shell
			dependencies.setItemLabelConverter(
					dto -> Commands.shellifyCommandName(dto.getName()));
		}

		dependencies.setValueConverter(s -> {
			for (SpringBootDependencyDTO dto : choices) {
				if (dto.getId().equals(s)) {
					return dto;
				}
			}
			return null;
		});

		builder.add(springBootVersion).add(dependencies);
	}

	private List<SpringBootDependencyDTO> initDependencies() throws Exception {
		List<SpringBootDependencyDTO> list = new ArrayList<>();

		for (Object dep : fetchDependencies()) {
			Map group = (Map) dep;
			String groupName = (String) group.get("name");
			List content;
			// Add this test as the json file & yaml file uses a different key
			if (group.get("content") != null) {
				content = (List) group.get("content");
			} else {
				content = (List) group.get("values");
			}
			for (Object row : content) {
				Map item = (Map) row;
				String id = (String) item.get("id");
				String name = (String) item.get("name");
				String description = (String) item.get("description");
				list.add(new SpringBootDependencyDTO(groupName, id, name, description));
			}
		}

		return list;
	}

	@Override
	protected boolean isProjectRequired() {
		return false;
	}

	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.from(super.getMetadata(context), getClass())
				.category(Categories.create(CATEGORY)).name(CATEGORY + ": New Project")
				.description("Create a new Spring Boot project");
	}

	@Override
	public Result execute(UIExecutionContext context) throws Exception {
		UIContext uiContext = context.getUIContext();

		Project project = (Project) uiContext.getAttributeMap().get(Project.class);
		if (project == null) {
			project = getSelectedProject(context.getUIContext());
		}
		MetadataFacet metadataFacet = project.getFacet(MetadataFacet.class);

		String projectName = metadataFacet.getProjectName();
		String groupId = metadataFacet.getProjectGroupName();
		String version = metadataFacet.getProjectVersion();
		File folder = project.getRoot().reify(DirectoryResource.class)
				.getUnderlyingResourceObject();

		Map<String, SpringBootDependencyDTO> selectedDTOs = new HashMap<>();
		int[] selected = dependencies.getSelectedIndexes();
		CollectionStringBuffer csbSpringBoot = new CollectionStringBuffer(",");
		CollectionStringBuffer csbFabric8 = new CollectionStringBuffer(",");
		for (int val : selected) {
			SpringBootDependencyDTO dto = choices.get(val);
			csbSpringBoot.append(dto.getId());
			selectedDTOs.put(dto.getId(), dto);
		}
		String springBootDeps = csbSpringBoot.toString();

		// boot version need the RELEASE suffix
		String bootVersion = springBootVersion.getValue() + ".RELEASE";

		String url = String
				.format("%s?bootVersion=%s&groupId=%s&artifactId=%s&version=%s&packageName=%s&dependencies=%s",
						STARTER_ZIP_URL, bootVersion, groupId, projectName, version, groupId,
						springBootDeps);

		LOG.info("About to query url: " + url);
		uiOutput.info(uiOutput.out(), "About to query url: " + url);

		// use http client to call start.spring.io that creates the project
		OkHttpClient client = createOkHttpClient();
		Request request = new Request.Builder().url(url).build();
		Response response = client.newCall(request).execute();
		InputStream is = response.body().byteStream();

		// some archetypes might not use maven or use the maven source layout so lets remove
		// the pom.xml and src folder if its already been pre-created
		// as these will be created if necessary via the archetype jar's contents
		File pom = new File(folder, "pom.xml");
		if (pom.isFile() && pom.exists()) {
			pom.delete();
		}
		File src = new File(folder, "src");
		if (src.isDirectory() && src.exists()) {
			recursiveDelete(src);
		}

		File name = new File(folder, projectName + ".zip");
		if (name.exists()) {
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

	private List fetchDependencies() throws Exception {
		List<Map> deps;

		// Check if we have a Spring Boot Config File
		if (SPRING_BOOT_CONFIG_FILE != null) {
			Yaml yaml = new Yaml();
			InputStream input = new URL(SPRING_BOOT_CONFIG_FILE).openStream();
			//uiOutput.info(uiOutput.out(),"Will use the Spring Boot Config file : " + SPRING_BOOT_CONFIG_FILE);
			Map data = (Map) yaml.load(input);
			Map initializer = (Map) data.get("initializr");
			deps = (List) initializer.get("dependencies");
		} else {
			// Fetch the dependencies list from the start.spring.io server
			OkHttpClient client = createOkHttpClient();
			Request request = new Request.Builder().url(STARTER_URL).build();
			Response response = client.newCall(request).execute();
			// uiOutput.info(uiOutput.out(),"Response received from starter web server : " + response.code());
			Map data = jsonToMap(response.body().string());
			Map dependencies = (Map) data.get("dependencies");
			deps = (List) dependencies.get("values");
			//uiOutput.error(uiOutput.err(),"Dependencies : " + deps);
		}
		return deps;
	}

	private Map<String,Object> jsonToMap(String content) throws IOException {
		HashMap<String,Object> result = new ObjectMapper().readValue(content, HashMap.class);
		JSONObject jObject = new JSONObject(result);
		return toMap(jObject);
	}

	private Map<String, Object> toMap(JSONObject object) throws JSONException {
		Map<String, Object> map = new HashMap<String, Object>();

		Iterator<String> keysItr = object.keys();
		while(keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = object.get(key);

			if(value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if(value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			map.put(key, value);
		}
		return map;
	}

	private List<Object> toList(JSONArray array) throws JSONException {
		List<Object> list = new ArrayList<Object>();
		for(int i = 0; i < array.length(); i++) {
			Object value = array.get(i);
			if(value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if(value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			list.add(value);
		}
		return list;
	}

}
