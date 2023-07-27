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

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.samples.petclinic.system.BasePage;
import org.springframework.samples.petclinic.system.Form;
import org.springframework.samples.petclinic.system.InputField;
import org.springframework.samples.petclinic.system.SelectField;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import io.jstach.jstache.JStache;
import io.jstach.jstache.JStachePartial;
import io.jstach.jstache.JStachePartials;
import io.jstach.opt.spring.webmvc.JStachioModelView;
import jakarta.validation.Valid;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
@RequestMapping("/owners/{ownerId}")
class PetController {

	private final PetRepository pets;

	private final OwnerRepository owners;

	public PetController(PetRepository pets, OwnerRepository owners) {
		this.pets = pets;
		this.owners = owners;
	}

	@ModelAttribute("types")
	public Collection<PetType> populatePetTypes() {
		return this.pets.findPetTypes();
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable("ownerId") int ownerId) {
		return this.owners.findById(ownerId);
	}

	@ModelAttribute("pet")
	public Pet findPet(@PathVariable("ownerId") int ownerId,
			@PathVariable(name = "petId", required = false) Integer petId) {
		return petId == null ? new Pet() : this.owners.findById(ownerId).getPet(petId);
	}

	@InitBinder("owner")
	public void initOwnerBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@InitBinder("pet")
	public void initPetBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new PetValidator());
		dataBinder.setDisallowedFields("id");
	}

	@GetMapping("/pets/new")
	public View initCreationForm(Owner owner, Pet pet, ModelMap model) {
		owner.addPet(pet);
		return JStachioModelView.of(new PetPage(owner, pet, (Collection<PetType>) model.get("types")));
	}

	@PostMapping("/pets/new")
	public View processCreationForm(Owner owner, @Valid Pet pet, BindingResult result, ModelMap model) {
		if (StringUtils.hasLength(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null) {
			result.rejectValue("name", "duplicate", "already exists");
		}
		if (result.hasErrors()) {
			return initCreationForm(owner, pet, model);
		}
		else {
			this.owners.save(owner);
			return new RedirectView("/owners/" + owner.getId());
		}
	}

	@GetMapping("/pets/{petId}/edit")
	public View initUpdateForm(Owner owner, Pet pet, ModelMap model) {
		return initCreationForm(owner, pet, model);
	}

	@PostMapping("/pets/{petId}/edit")
	public View processUpdateForm(@Valid Pet pet, BindingResult result, Owner owner, ModelMap model) {
		if (result.hasErrors()) {
			return initCreationForm(owner, pet, model);
		}
		else {
			this.owners.save(owner);
			return new RedirectView("/owners/" + owner.getId());
		}
	}

}

@JStache(path = "pets/createOrUpdatePetForm")
@JStachePartials(@JStachePartial(name = "inputField", path = "fragments/inputField"))
class PetPage extends BasePage {

	final Pet pet;

	final Owner owner;

	final Collection<PetType> types;

	PetPage(Owner owner, Pet pet, Collection<PetType> types) {
		this.pet = pet;
		this.owner = owner;
		this.types = types;
	}

	Form form() {
		return new Form("pet", this.pet);
	}

	String[] errors() {
		return status("pet").getErrorMessages();
	}

	InputField nameField() {
		return new InputField("Name", "name", this.pet.getName(), "text", status("pet", "name"));
	}

	InputField birthDate() {
		return new InputField("Birth Date", "birthDate", this.pet.getBirthDate().toString(), "date",
				status("pet", "birthDate"));
	}

	SelectField type() {
		return new SelectField("Type", "type", this.pet.getType() == null ? "" : this.pet.getType().toString(),
				this.types.stream().map(item -> item.toString()).collect(Collectors.toList()), status("pet", "type"));
	}

}
