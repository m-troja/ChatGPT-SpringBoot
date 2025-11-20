package com.michal.openai.mail.entity;

import com.michal.openai.functions.entity.ParameterProperties;
import com.michal.openai.functions.entity.ParameterPropertyAttribute;
import lombok.Data;

@Data
public class SendEmailParameterProperties implements ParameterProperties {
	private Subject subject;
	private Content content;
	private AddresseeEmail addresseeEmail;
	private AddresseeName addresseeName;
	
	public class Subject extends ParameterPropertyAttribute {
		public Subject() {}
		public Subject(String type, String description) {
			super(type, description);
		}
	}
	
	public class Content extends ParameterPropertyAttribute {
		public Content() {}
		public Content(String type, String description) {
			super(type, description);
		}
	}
	
	public class AddresseeEmail extends ParameterPropertyAttribute {
		public AddresseeEmail() {}
		public AddresseeEmail(String type, String description) {
			super(type, description);
		}
	}
	
	public class AddresseeName extends ParameterPropertyAttribute {
		public AddresseeName() {}
		public AddresseeName(String type, String description) {
			super(type, description);
		}
	}
}