package org.abogdanov.university.domain;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.CascadeType;

@Entity
@Table(name = "TEACHERS")
public class Teacher {
	private int id;
	private String firstName;
	private String lastName;
	private Dept dept;

	public Teacher() {
	}

	public Teacher(String firstName, String lastName, Dept dept) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.dept = dept;
	}

	@Id
	@Column(name = "ID")
	@GeneratedValue
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "FIRST_NAME")
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Column(name = "LAST_NAME")
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, targetEntity = Dept.class)
	@JoinColumn(name = "DEPT_ID")
	public Dept getDept() {
		return dept;
	}

	public void setDept(Dept dept) {
		this.dept = dept;
	}
}
