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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template.Fragment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Utilities for rendering views of owners and pets.
 *
 * @author Dave Syer
 *
 */
@ControllerAdvice(assignableTypes = { OwnerController.class, PetController.class,
        VisitController.class })
class FormWidgetControllerAdvice {

    private static Pattern NAME = Pattern.compile(".*name=\\\"([a-zA-Z0-9]*)\\\".*");

    private static Pattern VALUES = Pattern.compile(".*values=\\\"([a-zA-Z0-9]*)\\\".*");

    private static Pattern TYPE = Pattern.compile(".*type=\\\"([a-zA-Z0-9]*)\\\".*");

    private final Mustache.Compiler compiler;

    @Autowired
    public FormWidgetControllerAdvice(Mustache.Compiler compiler) {
        this.compiler = compiler;
    }

    @ModelAttribute("form")
    public Map<String, Form> form(Map<String, Object> model) {
        return new LinkedHashMap<String, Form>() {
            @Override
            public boolean containsKey(Object key) {
                if (!super.containsKey(key)) {
                    put((String) key, new Form((String) key, model.get(key)));
                }
                return super.containsKey(key);
            }

            @Override
            public Form get(Object key) {
                if (!super.containsKey(key)) {
                    put((String) key, new Form((String) key, model.get(key)));
                }
                return super.get(key);
            }

        };
    }

    @ModelAttribute("inputField")
    public Mustache.Lambda inputField(Map<String, Object> model) {
        return (frag, out) -> {
            String body = frag.execute();
            String label = body.substring(0, body.indexOf("<") - 1).trim();
            Form form = (Form) frag.context();
            String target = form.getName();
            String name = match(NAME, body, "unknown");
            String type = match(TYPE, body, "text");
            BindingResult status = (BindingResult) model
                    .get("org.springframework.validation.BindingResult." + target);
            InputField field = new InputField(label, name, type, status);
            compiler.compile("{{>fragments/inputField}}").execute(field, out);
        };
    }

    @ModelAttribute("selectField")
    public Mustache.Lambda selectField(Map<String, Object> model) {
        return (frag, out) -> {
            String body = frag.execute();
            String label = body.substring(0, body.indexOf("<") - 1).trim();
            Form form = (Form) frag.context();
            String target = form.getName();
            String name = match(NAME, body, "unknown");
            String list = match(VALUES, body, "unknown");
            List<String> values = new ArrayList<String>();
            @SuppressWarnings("unchecked")
            List<String> valuesAttr = (List<String>) model.get(list);
            for (Object value : valuesAttr) {
                values.add(value.toString());
            }
            BindingResult status = (BindingResult) model
                    .get("org.springframework.validation.BindingResult." + target);
            SelectField field = new SelectField(label, name, values, status);
            compiler.compile("{{>fragments/selectField}}").execute(field, out);
        };
    }

    private String match(Pattern pattern, String body, String fallback) {
        Matcher matcher = pattern.matcher(body);
        return matcher.matches() ? matcher.group(1) : fallback;
    }

}

class Form implements Mustache.Lambda {

    private Object target;

    private String name;

    public Form(String name, Object target) {
        this.name = name;
        this.target = target;
    }

    @Override
    public void execute(Fragment frag, Writer out) throws IOException {
        frag.execute(this, out);
    }

    public Object getTarget() {
        return target;
    }

    public String getName() {
        return name;
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
            errors = status.getFieldErrors(name).stream()
                    .map(error -> error.getDefaultMessage()).collect(Collectors.toList());
            value = status.getFieldValue(name) == null ? ""
                    : status.getFieldValue(name).toString();
        }
        this.date = "date".equals(type);
    }

}

class SelectField {

    String label;

    String name;

    boolean valid;

    String value;

    List<String> errors = Collections.emptyList();

    List<Option> values = new ArrayList<Option>();

    public SelectField(String label, String name, List<String> values,
            BindingResult status) {
        this.label = label;
        this.name = name;
        if (status != null) {
            valid = !status.hasFieldErrors(name);
            errors = status.getFieldErrors(name).stream()
                    .map(error -> error.getDefaultMessage()).collect(Collectors.toList());
            value = status.getFieldValue(name) == null ? ""
                    : status.getFieldValue(name).toString();
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
