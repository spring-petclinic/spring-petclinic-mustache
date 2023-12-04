package org.springframework.samples.petclinic.system;

import org.springframework.web.servlet.support.BindStatus;

public class InputField implements FormField {

	public String label;

	public String name;

	public boolean date;

	public boolean valid = true;

	public String value;

	public String[] errors = new String[0];

	public InputField(String label, String name, String value, String type) {
		this.label = label;
		this.name = name;
		this.value = value == null ? "" : value;
		this.date = "date".equals(type);
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