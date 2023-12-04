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
import org.springframework.web.servlet.support.RequestContext;

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

	private Collection<PetType> populatePetTypes() {
		return this.pets.findPetTypes();
	}

	@ModelAttribute("pet")
	public Pet findPet(@PathVariable(name = "petId", required = false) Integer petId) {
		return petId == null ? new Pet() : pets.findById(petId);
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable("ownerId") int ownerId) {
		return this.owners.findById(ownerId);
	}

	@InitBinder("owner")
	public void initOwnerBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@InitBinder("pet")
	public void initPetBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
		dataBinder.setValidator(new PetValidator());
	}

	@GetMapping("/pets/new")
	public String initCreationForm(Owner owner, Pet pet, ModelMap model) {
		owner.addPet(pet);
		model.addAttribute("form", new PetForm(pet, populatePetTypes()));
		return "pets/createOrUpdatePetForm";
	}

	@PostMapping("/pets/new")
	public String processCreationForm(Owner owner, @Valid Pet pet, BindingResult result, ModelMap model) {
		if (StringUtils.hasLength(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null) {
			result.rejectValue("name", "duplicate", "already exists");
		}
		owner.addPet(pet);
		if (result.hasErrors()) {
			model.put("pet", pet);
			return initCreationForm(owner, pet, model);
		}
		else {
			this.pets.save(pet);
			return "redirect:/owners/{ownerId}";
		}
	}

	@GetMapping("/pets/{petId}/edit")
	public String initUpdateForm(Owner owner, Pet pet, ModelMap model) {
		return initCreationForm(owner, pet, model);
	}

	@PostMapping("/pets/{petId}/edit")
	public String processUpdateForm(@Valid Pet pet, BindingResult result, Owner owner, ModelMap model) {
		if (result.hasErrors()) {
			pet.setOwner(owner);
			model.put("pet", pet);
			return initCreationForm(owner, pet, model);
		}
		else {
			owner.addPet(pet);
			this.pets.save(pet);
			return "redirect:/owners/{ownerId}";
		}
	}

	static class PetForm implements Form {

		final Pet pet;

		final Collection<PetType> types;

		private InputField nameField;

		private InputField birthDate;

		private SelectField type;

		PetForm(Pet pet, Collection<PetType> types) {
			this.pet = pet;
			this.types = types;
			nameField = new InputField("Name", "name", this.pet.getName(), "text");
			birthDate = new InputField("Birth Date", "birthDate", this.pet.getBirthDate().toString(), "date");
			type = new SelectField("Type", "type", this.pet.getType() == null ? "" : this.pet.getType().toString(),
					this.types.stream().map(item -> item.toString()).collect(Collectors.toList()));
		}

		InputField nameField() {
			return nameField;
		}

		InputField birthDate() {
			return birthDate;
		}

		SelectField type() {
			return type;
		}

		@Override
		public void setContext(RequestContext context) {
			nameField.setStatus(context.getBindStatus("pet.name"));
			birthDate.setStatus(context.getBindStatus("pet.birthDate"));
			type.setStatus(context.getBindStatus("pet.type"));
		}

	}

}
