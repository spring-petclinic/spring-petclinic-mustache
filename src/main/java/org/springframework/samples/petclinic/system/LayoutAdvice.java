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

import java.util.HashSet;
import java.util.Map;

import org.springframework.lang.Nullable;
import org.springframework.samples.petclinic.system.Application.Menu;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.support.RequestContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Utilities for rendering the HTML layout (menus, logs etc.)
 *
 * @author Dave Syer
 *
 */
@Component
public class LayoutAdvice implements HandlerInterceptor, WebMvcConfigurer {

	private Application application;

	public LayoutAdvice(Application application) {
		this.application = application;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		return HandlerInterceptor.super.preHandle(request, response, handler);
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable ModelAndView modelAndView) throws Exception {
		for (Menu menu : application.getMenus()) {
			menu.setActive(false);
		}
		if (modelAndView != null) {
			Map<String, Object> map = modelAndView.getModel();
			if (map.containsKey("owner") || map.containsKey("owners")) {
				application.getMenu("owners").setActive(true);
			} else if (map.containsKey("vets")) {
				application.getMenu("vets").setActive(true);
			} else {
				application.getMenu("home").setActive(true);
			}
			modelAndView.addObject("menus", application.getMenus());
			RequestContext context = new RequestContext(request, map);
			for (String key : new HashSet<>(map.keySet())) {
				if (key.startsWith("org.springframework.validation.BindingResult.")) {
					String name = key.substring(key.lastIndexOf(".")+1);
					modelAndView.addObject("errors", context.getBindStatus(name + ".*").getErrorMessages());
				}
			}
		}
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(this);
	}

}
