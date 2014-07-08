package org.abogdanov.university.domain;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;

@Entity
@Table(name = "SUBJECTS")
public class Subject {
	private int id;
	private String name;
	private int hours;

	public Subject() {
	}

	public Subject(String name, int hours) {
		this.name = name;
		this.hours = hours;
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

	@Column(name = "NAME")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "HOURS")
	public int getHours() {
		return hours;
	}

	public void setHours(int hours) {
		this.hours = hours;
	}
}
