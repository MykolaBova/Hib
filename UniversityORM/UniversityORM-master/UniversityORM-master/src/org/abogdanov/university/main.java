package org.abogdanov.university;

import java.sql.SQLException;
import java.util.List;

import org.abogdanov.university.domain.*;
import org.abogdanov.university.dao.Factory;


public class Main {
	public static void main(String[] args) throws SQLException {
		//Создадим двух студентов
		Student s1 = new Student("Artem", "Ivanov", 21);
		Student s2 = new Student("Alisa", "Petrova", 24);

		Dept d1 = new Dept("Software Engineering");
		Dept d2 = new Dept("Nonlinear Equations");

		Teacher t1 = new Teacher("Alexander", "Makhortov", d1);
		Teacher t2 = new Teacher("Vladimir", "Zadorozhny", d2);

		Subject subj1 = new Subject("Diff Equations", 300);
		Subject subj2 = new Subject("IT", 200);

		Exam e1 = new Exam(s1, t1, subj2, 5);
		Exam e2 = new Exam(s2, t2, subj1, 3);


		//Сохраним их в бд, id будут сгенерированы автоматически
		Factory.getInstance().getStudentDAO().add(s1);
		Factory.getInstance().getStudentDAO().add(s2);

		Factory.getInstance().getDeptDAO().add(d1);
		Factory.getInstance().getDeptDAO().add(d2);

		Factory.getInstance().getTeacherDAO().add(t1);
		Factory.getInstance().getTeacherDAO().add(t2);

		Factory.getInstance().getSubjectDAO().add(subj1);
		Factory.getInstance().getSubjectDAO().add(subj2);

		Factory.getInstance().getExamDAO().add(e1);
		Factory.getInstance().getExamDAO().add(e2);

		//Выведем всех студентов из бд
		List<Student> studs = Factory.getInstance().getStudentDAO().getAll();
		System.out.println("\n\n\tВсе студенты\n=============================");
		for (int i = 0; i < studs.size(); ++i) {
			System.out.println("Имя: " +
					studs.get(i).getFirstName() +
					"\nФамилия: " +
					studs.get(i).getLastName() +
					"\nВозраст : " +
					studs.get(i).getAge() +
					"\nid : " +
					studs.get(i).getId());
			System.out.println("=============================");
		}

		//Выведем всех преподавателей из бд
		List<Teacher> teachers = Factory.getInstance().getTeacherDAO().getAll();
		System.out.println("\n\n\tВсе преподаватели\n=============================");
		for (Teacher teacher : teachers) {
			System.out.println("Имя: " +
					teacher.getFirstName() +
					"\nФамилия: " +
					teacher.getLastName() +
					"\nid: " +
					teacher.getId());
			System.out.println("=============================");
		}

		//Выведем все кафедры из бд
		List<Dept> depts = Factory.getInstance().getDeptDAO().getAll();
		System.out.println("\n\n\tВсе кафедры\n=============================");
		for (Dept dept : depts) {
			System.out.println("Название: " +
					dept.getName() +
					"\nid: " +
					dept.getId()
			);
			System.out.println("=============================");
		}

		//Выведем все предметы из бд
		List<Subject> subjects = Factory.getInstance().getSubjectDAO().getAll();
		System.out.println("\n\n\tВсе предметы\n=============================");
		for (Subject subj : subjects) {
			System.out.println("Название: " +
					subj.getName() +
					"\nЧасов: " +
					subj.getHours() +
					"\nid: " +
					subj.getId()
			);
			System.out.println("=============================");
		}

		//Выведем все экзамены из бд
		List<Exam> exams = Factory.getInstance().getExamDAO().getAll();
		System.out.println("\n\n\tВсе экзамены\n=============================");
		for (Exam exam : exams) {
			System.out.println("Студент: \n\tИмя: " +
					exam.getStudent().getFirstName() +
					"\n\tФамилия: " +
					exam.getStudent().getLastName() +
					"\nПреподаватель: \n\tИмя: " +
					exam.getTeacher().getFirstName() +
					"\n\tФамилия: " +
					exam.getTeacher().getLastName() +
					"\nПредмет: \n\tНазвание: " +
					exam.getSubject().getName() +
					"\n\tОценка: " +
					exam.getGrade()
			);
			System.out.println("=============================");
		}
	}
}
