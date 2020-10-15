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
package org.springframework.samples.petclinic.owner;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Template.Fragment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Utilities for rendering views of owners and pets.
 *
 * @author Dave Syer
 *
 */
@ControllerAdvice(assignableTypes = { OwnerController.class, PetController.class, VisitController.class })
class FormWidgetControllerAdvice {

	private final Mustache.Compiler compiler;

	@Autowired
	public FormWidgetControllerAdvice(Mustache.Compiler compiler) {
		this.compiler = compiler;
	}

	@ModelAttribute("inputField")
	public Mustache.Lambda inputField(Map<String, Object> status) {
		return new InputFieldHandler(compiler, status);
	}

	@ModelAttribute("selectField")
	public Mustache.Lambda selectField(Map<String, Object> status) {
		return new SelectFieldHandler(compiler, status);
	}

}

class InputField {

	String label;

	String name;

	boolean date;

	boolean valid;

	String value;

	List<String> errors = Collections.emptyList();

	public InputField(String label, String name, String type, BindingResult status) {
		this.label = label;
		this.name = name;
		if (status != null) {
			valid = !status.hasFieldErrors(name);
			errors = status.getFieldErrors(name).stream().map(error -> error.getDefaultMessage())
					.collect(Collectors.toList());
			value = status.getFieldValue(name) == null ? "" : status.getFieldValue(name).toString();
		}
		this.date = "date".equals(type);
	}

}

class InputFieldHandler implements Mustache.Lambda {

	private Compiler compiler;

	private Map<String, Object> model;

	public InputFieldHandler(Compiler compiler, Map<String, Object> model) {
		this.compiler = compiler;
		this.model = model;
	}

	@Override
	public void execute(Fragment frag, Writer out) throws IOException {
		String body = frag.execute();
		String[] tokens = StringUtils.commaDelimitedListToStringArray(body);
		String target = tokens[0].trim();
		String label = tokens[1].trim();
		String name = tokens[2].trim();
		String type = tokens[3].trim();
		BindingResult status = (BindingResult) model.get("org.springframework.validation.BindingResult." + target);
		InputField field = new InputField(label, name, type, status);
		compiler.compile("{{>fragments/inputField}}").execute(field, out);
	}

}

class SelectField {

	String label;

	String name;

	boolean valid;

	String value;

	List<String> errors = Collections.emptyList();

	List<Option> values = new ArrayList<Option>();

	public SelectField(String label, String name, List<String> values, BindingResult status) {
		this.label = label;
		this.name = name;
		if (status != null) {
			valid = !status.hasFieldErrors(name);
			errors = status.getFieldErrors(name).stream().map(error -> error.getDefaultMessage())
					.collect(Collectors.toList());
			value = status.getFieldValue(name) == null ? "" : status.getFieldValue(name).toString();
		}
		for (String value : values) {
			this.values.add(new Option(value, value.equals(this.value)));
		}
	}

	static class Option {

		public Option(String value, boolean selected) {
			this.value = value;
			this.selected = selected;
		}

		String value;

		boolean selected;

	}

}

class SelectFieldHandler implements Mustache.Lambda {

	private Compiler compiler;

	private Map<String, Object> model;

	public SelectFieldHandler(Compiler compiler, Map<String, Object> model) {
		this.compiler = compiler;
		this.model = model;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(Fragment frag, Writer out) throws IOException {
		String body = frag.execute();
		String[] tokens = StringUtils.commaDelimitedListToStringArray(body);
		String target = tokens[0].trim();
		String label = tokens[1].trim();
		String name = tokens[2].trim();
		List<String> values = new ArrayList<String>();
		for (Object value : (List<String>) model.get(tokens[3].trim())) {
			values.add(value.toString());
		}
		BindingResult status = (BindingResult) model.get("org.springframework.validation.BindingResult." + target);
		SelectField field = new SelectField(label, name, values, status);
		compiler.compile("{{>fragments/selectField}}").execute(field, out);
	}

}