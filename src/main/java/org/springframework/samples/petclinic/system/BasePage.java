package org.springframework.samples.petclinic.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.system.Application.Menu;
import org.springframework.validation.BindingResult;

public class BasePage {

	private Application application;

	private Map<String, BindingResult> status = new HashMap<>();

	private String active = "home";

	@Autowired
	public void setApplication(Application application) {
		this.application = application;
	}

	public void setStatus(String name, BindingResult status) {
		this.status.put(name, status);
	}

	public void activate(String name) {
		this.active = name;
	}

	public List<Menu> getMenus() {
		Menu menu = application.getMenu(active);
		if (menu != null) {
			application.getMenus().forEach(m -> m.setActive(false));
			menu.setActive(true);
		}
		return application.getMenus();
	}

	public BindingResult status(String name) {
		return this.status.get(name);
	}

}
