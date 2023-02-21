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

import java.util.Map;

import org.springframework.samples.petclinic.mustache.PageConfigurer;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utilities for rendering the HTML layout (menus, logs etc.)
 *
 * @author Dave Syer
 *
 */
@ControllerAdvice
public class ApplicationPageConfigurer implements PageConfigurer {

	private final Application application;

	public ApplicationPageConfigurer(Application application) {
		this.application = application;
	}

	@Override
	public void configure(Object page, Map<String, ?> model, HttpServletRequest request) {
		if (page instanceof BasePage) {
			BasePage base = (BasePage) page;
			for (String key : model.keySet()) {
				if (key.startsWith(BindingResult.MODEL_KEY_PREFIX)) {
					String name = key.substring(BindingResult.MODEL_KEY_PREFIX.length());
					base.setStatus(name, (BindingResult) model.get(key));
				}
			}
			base.setApplication(application);
		}
	}

}
