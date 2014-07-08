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
@Table(name = "EXAMS")
public class Exam {
	private int id;
	private Student student;
	private Teacher teacher;
	private Subject subject;
	private int grade;

	public Exam() {
	}

	public Exam(Student student, Teacher teacher, Subject subject, int grade) {
		this.student = student;
		this.teacher = teacher;
		this.subject = subject;
		this.grade = grade;
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

	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, targetEntity = Student.class)
	@JoinColumn(name = "STUDENT_ID")
	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, targetEntity = Teacher.class)
	@JoinColumn(name = "TEACHER_ID")
	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}

	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, targetEntity = Subject.class)
	@JoinColumn(name = "SUBJECT_ID")
	public Subject getSubject() {
		return subject;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	@Column(name = "GRADE")
	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}
}
