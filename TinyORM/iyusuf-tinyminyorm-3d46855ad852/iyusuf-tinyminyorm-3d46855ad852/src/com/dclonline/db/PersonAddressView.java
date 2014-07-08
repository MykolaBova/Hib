package com.dclonline.db;

public class PersonAddressView implements TableDefinition{
	private String lastName;
	private String firstName;
	private String zipCode;
	
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	@Override
	public String toString() {
		return "ViewPersonAddress [lastName=" + lastName + ", firstName="
				+ firstName + ", zipCode=" + zipCode + "]";
	}
	
}
