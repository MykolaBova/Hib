package org.abogdanov.university.dao.impl;

import org.abogdanov.university.dao.GenericDAO;
import org.abogdanov.university.util.HibernateUtil;

import org.hibernate.Session;

import javax.swing.*;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GenericDAOImpl<E>
		implements GenericDAO<E> {

	// Hack to make Reified Generics for Java
	//
	private Class<E> persistentClass;

	public GenericDAOImpl() {
		this.persistentClass = (Class<E>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	public Class<E> getPersistentClass() {
		return persistentClass;
	}
	//
	// Hack to make Reified Generics for Java

	public void add(E thing) throws SQLException {
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.save(thing);
			session.getTransaction().commit();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Ошибка I/O", JOptionPane.ERROR_MESSAGE);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	public void update(E thing) throws SQLException {
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.update(thing);
			session.getTransaction().commit();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Ошибка I/O", JOptionPane.ERROR_MESSAGE);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	public E getById(int id) throws SQLException {
		Session session = null;
		E thing = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			thing = (E) session.load(getPersistentClass(), id);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Ошибка I/O", JOptionPane.ERROR_MESSAGE);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		return thing;
	}

	public List<E> getAll() throws SQLException {
		Session session = null;
		List<E> things = new ArrayList<E>();
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			things = session.createCriteria(getPersistentClass()).list();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Ошибка I/O", JOptionPane.ERROR_MESSAGE);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		return things;
	}

	public void delete(E thing) throws SQLException {
		Session session = null;
		try {
			session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			session.delete(thing);
			session.getTransaction().commit();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Ошибка I/O", JOptionPane.ERROR_MESSAGE);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}
}
