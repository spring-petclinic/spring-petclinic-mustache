/*
 * Copyright 2019-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.system;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Template.Fragment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.samples.petclinic.system.Application.Menu;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Utilities for rendering the HTML layout (menus, logs etc.)
 *
 * @author Dave Syer
 *
 */
@ControllerAdvice
public class LayoutAdvice {

	private final Mustache.Compiler compiler;

	private Application application;

	@Autowired
	public LayoutAdvice(Mustache.Compiler compiler, Application application) {
		this.compiler = compiler;
		this.application = application;
	}

	@ModelAttribute("menus")
	public Iterable<Menu> menus(@ModelAttribute Layout layout) {
		for (Menu menu : application.getMenus()) {
			menu.setActive(false);
		}
		return application.getMenus();
	}

	@ModelAttribute("menu")
	public Mustache.Lambda menu(@ModelAttribute Layout layout) {
		return (frag, out) -> {
			Menu menu = application.getMenu(frag.execute());
			menu.setActive(true);
			layout.title = menu.getTitle();
		};
	}

	@ModelAttribute("layout")
	public Mustache.Lambda layout(Map<String, Object> model) {
		return new Layout(compiler);
	}

}

@Component
@ConfigurationProperties("app")
class Application {

	private List<Menu> menus = new ArrayList<>();

	public List<Menu> getMenus() {
		return menus;
	}

	public static class Menu {

		private String name;

		private String path;

		private String title;

		private boolean active;

		private String glyph;

		public String getGlyph() {
			return glyph;
		}

		public void setGlyph(String glyph) {
			this.glyph = glyph;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public boolean isActive() {
			return active;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

	}

	public Menu getMenu(String name) {
		for (Menu menu : menus) {
			if (menu.getName().equalsIgnoreCase(name)) {
				return menu;
			}
		}
		return menus.get(0);
	}

}

class Layout implements Mustache.Lambda {

	String title = "Spring PetClinic";

	String body;

	private Compiler compiler;

	public Layout(Compiler compiler) {
		this.compiler = compiler;
	}

	@Override
	public void execute(Fragment frag, Writer out) throws IOException {
		body = frag.execute();
		compiler.compile("{{>fragments/layout}}").execute(frag.context(), out);
	}

}