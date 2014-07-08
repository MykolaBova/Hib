package org.abogdanov.university.domain;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;


@Entity
@Table(name = "DEPTS")
public class Dept {
	private int id;
	private String name;

	public Dept() {
	}

	public Dept(String name) {
		this.name = name;
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

}
