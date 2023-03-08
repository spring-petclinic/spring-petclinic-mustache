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
package org.springframework.samples.petclinic.owner;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.system.BasePage;
import org.springframework.samples.petclinic.system.Form;
import org.springframework.samples.petclinic.system.InputField;
import org.springframework.samples.petclinic.system.PagedModelPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import io.jstach.jstache.JStache;
import io.jstach.jstache.JStacheLambda;
import io.jstach.jstache.JStachePartial;
import io.jstach.jstache.JStachePartials;
import io.jstach.jstache.JStacheLambda.Raw;
import io.jstach.jstachio.JStachio;
import io.jstach.opt.spring.webmvc.JStachioModelView;
import jakarta.validation.Valid;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@Controller
class OwnerController {

	private final OwnerRepository owners;

	public OwnerController(OwnerRepository clinicService) {
		this.owners = clinicService;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable(name = "ownerId", required = false) Integer ownerId) {
		return ownerId == null ? new Owner() : this.owners.findById(ownerId);
	}

	@GetMapping("/owners/new")
	public View initCreationForm(Owner owner) {
		return JStachioModelView.of(new EditOwnerPage(owner));
	}

	@PostMapping("/owners/new")
	public View processCreationForm(@Valid Owner owner, BindingResult result) {
		if (result.hasErrors()) {
			return initCreationForm(owner);
		}
		else {
			this.owners.save(owner);
			return new RedirectView("/owners/" + owner.getId());
		}
	}

	@GetMapping("/owners/find")
	public View initFindForm(Owner owner) {
		return JStachioModelView.of(new FindOwnerPage(owner));
	}

	@GetMapping("/owners")
	public View processFindForm(@RequestParam(defaultValue = "1") int page, Owner owner, BindingResult result,
			Model model) {

		// allow parameterless GET request for /owners to return all records
		if (owner.getLastName() == null) {
			owner.setLastName(""); // empty string signifies broadest possible search
		}

		// find owners by last name
		String lastName = owner.getLastName();
		Page<Owner> ownersResults = findPaginatedForOwnersLastName(page, lastName);
		if (ownersResults.isEmpty()) {
			// no owners found
			result.rejectValue("lastName", "notFound", "not found");
			return JStachioModelView.of(new FindOwnerPage(owner));
		}
		else if (ownersResults.getTotalElements() == 1) {
			// 1 owner found
			owner = ownersResults.iterator().next();
			return new RedirectView("/owners/" + owner.getId());
		}
		else {
			// multiple owners found
			return JStachioModelView.of(new OwnersPage(page, ownersResults));
		}
	}

	private Page<Owner> findPaginatedForOwnersLastName(int page, String lastname) {

		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return owners.findByLastName(lastname, pageable);

	}

	@GetMapping("/owners/{ownerId}/edit")
	public View initUpdateOwnerForm(@PathVariable("ownerId") int ownerId, Model model) {
		Owner owner = this.owners.findById(ownerId);
		model.addAttribute(owner);
		return initCreationForm(owner);
	}

	@PostMapping("/owners/{ownerId}/edit")
	public View processUpdateOwnerForm(@Valid Owner owner, BindingResult result, @PathVariable("ownerId") int ownerId) {
		if (result.hasErrors()) {
			return initCreationForm(owner);
		}
		else {
			owner.setId(ownerId);
			this.owners.save(owner);
			return new RedirectView("/owners/" + owner.getId());
		}
	}

	/**
	 * Custom handler for displaying an owner.
	 * @param ownerId the ID of the owner to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/owners/{ownerId}")
	public ModelAndView showOwner(@PathVariable("ownerId") int ownerId) {
		Owner owner = this.owners.findById(ownerId);
		ModelAndView mav = new ModelAndView(JStachioModelView.of(new OwnerPage(owner)));
		mav.addObject(owner);
		return mav;
	}

}

@JStache(path = "owners/findOwners")
class FindOwnerPage extends BasePage {

	final Owner owner;

	FindOwnerPage(Owner owner) {
		this.owner = owner;
	}

	Form form() {
		return new Form("owner", this.owner);
	}

	String[] errors() {
		return status("owner").getErrorMessages();
	}

}

@JStache(path = "owners/createOrUpdateOwnerForm")
@JStachePartials(@JStachePartial(name = "inputField", path="fragments/inputField"))
class EditOwnerPage extends BasePage {

	final Owner owner;

	EditOwnerPage(Owner owner) {
		this.owner = owner;
	}

	Form form() {
		return new Form("owner", this.owner);
	}

	String[] errors() {
		return status("owner").getErrorMessages();
	}

	InputField firstName() {
		return new InputField("First Name", "firstName", this.owner.getFirstName(), "text",
				status("owner", "firstName"));
	}

	InputField lastName() {
		return new InputField("Last Name", "lastName", this.owner.getLastName(), "text", status("owner", "lastName"));
	}

	InputField address() {
		return new InputField("Address", "address", this.owner.getAddress(), "text", status("owner", "address"));
	}

	InputField city() {
		return new InputField("City", "city", this.owner.getCity(), "text", status("owner", "city"));
	}

	InputField telephone() {
		return new InputField("Telephone", "telephone", this.owner.getTelephone(), "text",
				status("owner", "telephone"));
	}

}

@JStache(path = "owners/ownerDetails")
class OwnerPage extends BasePage {

	final Owner owner;

	OwnerPage(Owner owner) {
		this.owner = owner;
	}

}

@JStache(path = "owners/ownersList")
class OwnersPage extends PagedModelPage<Owner> {

	OwnersPage(int page, Page<Owner> paginated) {
		super(page, paginated);
	}

	List<Owner> listOwners() {
		return list();
	}

}
