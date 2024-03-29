package org.springframework.samples.petclinic.system;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.servlet.support.BindStatus;

public class SelectField implements FormField {

	public String label;

	public String name;

	public boolean date;

	public boolean valid = true;

	public String value;

	public List<SelectValue> values = new ArrayList<>();

	public String[] errors = new String[0];

	public SelectField(String label, String name, String value, List<String> values) {
		this.label = label;
		this.name = name;
		this.value = value == null ? "" : value;
		for (String selection : values) {
			this.values.add(new SelectValue(selection, this.value.equals(selection)));
		}
	}

	@Override
	public void setStatus(BindStatus status) {
		if (status != null) {
			valid = !status.isError();
			errors = status.getErrorMessages();
			value = status.getValue() == null ? "" : status.getValue().toString();
		}
	}

}