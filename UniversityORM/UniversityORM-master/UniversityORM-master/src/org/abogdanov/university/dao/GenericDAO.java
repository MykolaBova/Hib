package org.abogdanov.university.dao;

import java.sql.SQLException;
import java.util.List;

// Паттерн DAO
public interface GenericDAO<E> {
	//Todo: add class-specific methods for inherited DAOs
	public void add(E thing) throws SQLException;        //добавить студента

	public void update(E thing) throws SQLException;    //обновить студента

	public E getById(int id) throws SQLException;        //получить студента по id

	public List<E> getAll() throws SQLException;            //получить всех студентов

	public void delete(E thing) throws SQLException;    //удалить студента

}
