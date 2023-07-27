/*
 * Copyright 2012-2019 the original author or authors.
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Test class for {@link CrashController}
 *
 * @author Colin But
 */
// Waiting https://github.com/spring-projects/spring-boot/issues/5574
@Disabled
@WebMvcTest(controllers = CrashController.class)
class CrashControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void testTriggerException() throws Exception {
		mockMvc.perform(get("/oups")).andExpect(view().name("exception"))
				.andExpect(model().attributeExists("exception")).andExpect(forwardedUrl("exception"))
				.andExpect(status().isOk());
	}

	@ControllerAdvice
	static class MockMvcValidationConfiguration {

		private final BasicErrorController errorController;

		MockMvcValidationConfiguration(BasicErrorController errorController) {
			this.errorController = errorController;
		}

		@ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
		ResponseEntity<?> defaultErrorHandler(HttpServletRequest request, Exception ex) {
			request.setAttribute("javax.servlet.error.request_uri", request.getPathInfo());
			request.setAttribute("javax.servlet.error.status_code", 400);
			return errorController.error(request);
		}

	}

}
