package com.nevexis.model.dao;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nevexis.connectionpool.ConnectionPoolCreator;
import com.nevexis.model.annotations.Entity;
import com.nevexis.model.annotations.Id;
import com.nevexis.model.annotations.Property;

public class MyEntityManager {

	public static <T> List<T> select(Class<T> clazz) throws SQLException {

		if (!clazz.isAnnotationPresent(Entity.class)) {
			return null;
		}

		Map<String, Field> fields = new HashMap<>();

		String entityName = clazz.getAnnotation(Entity.class).value();

		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(Id.class)) {
				fields.put("id", field);
			}
			if (field.isAnnotationPresent(Property.class)) {
				fields.put(field.getAnnotation(Property.class).value(), field);
			}
		}

		StringBuilder selectQuery = new StringBuilder("SELECT ");

		fields.forEach((fName, field) -> selectQuery.append(fName).append(", "));
		selectQuery.setLength(selectQuery.length() - 2);
		selectQuery.append(" FROM ").append(entityName);

		List<T> result = new ArrayList<>();
		try (Connection connection = ConnectionPoolCreator.getInstance().getConnection();
				PreparedStatement ps = connection.prepareStatement(selectQuery.toString())) {
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				T obj = clazz.getConstructor().newInstance();

				fields.forEach((fName, field) -> {
					field.setAccessible(true);

					try {
						field.set(obj, rs.getObject(fName));
					} catch (IllegalArgumentException | IllegalAccessException | SQLException e) {
						e.printStackTrace();
					}

					field.setAccessible(false);
				});

				result.add(obj);
			}

		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException ex) {
			ex.printStackTrace();
		}

		return result;
	}

	public static <T> boolean insert(T obj) throws SQLException {

		if (!obj.getClass().isAnnotationPresent(Entity.class)) {
			return false;
		}

		Map<String, Field> fields = new HashMap<>();

		String entityName = obj.getClass().getAnnotation(Entity.class).value();

		for (Field field : obj.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Property.class)) {
				fields.put(field.getAnnotation(Property.class).value(), field);
			}
		}

		StringBuilder insertQuery = new StringBuilder("INSERT into ");
		insertQuery.append(entityName).append(" ( ");

		fields.forEach((fName, field) -> insertQuery.append(fName).append(", "));
		insertQuery.setLength(insertQuery.length() - 2);
		insertQuery.append(" )");

		insertQuery.append(" VALUE ( ");

		fields.forEach((fName, field) -> {
			field.setAccessible(true);
			Object value = null;
			try {
				value = "String".equals(field.getType().getSimpleName()) ? "'" + field.get(obj) + "'" : field.get(obj);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			insertQuery.append(value).append(", ");
			field.setAccessible(false);
		});
		insertQuery.setLength(insertQuery.length() - 2);
		insertQuery.append(" )");

		int inserted = 0;
		try (Connection connection = ConnectionPoolCreator.getInstance().getConnection();
				PreparedStatement ps = connection.prepareStatement(insertQuery.toString())) {
			inserted = ps.executeUpdate();
		}

		return inserted > 0;
	}

	public static <T> boolean update(T obj) throws SQLException {

		if (!obj.getClass().isAnnotationPresent(Entity.class)) {
			return false;
		}

		Map<String, Field> fields = new HashMap<>();

		String entityName = obj.getClass().getAnnotation(Entity.class).value();

		for (Field field : obj.getClass().getDeclaredFields()) {
			if (field.isAnnotationPresent(Id.class)) {
				fields.put("id", field);
			}
			if (field.isAnnotationPresent(Property.class)) {
				fields.put(field.getAnnotation(Property.class).value(), field);
			}
		}

		Field objID = fields.get("id");
		objID.setAccessible(true);
		Object objIdValue = null;
		try {
			objIdValue = objID.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		objID.setAccessible(false);

		StringBuilder selectByIDQuery = new StringBuilder("SELECT ");

		fields.forEach((fName, field) -> selectByIDQuery.append(fName).append(", "));
		selectByIDQuery.setLength(selectByIDQuery.length() - 2);
		selectByIDQuery.append(" FROM ").append(entityName).append(" WHERE id = ").append(objIdValue);

		boolean update = false;
		try (Connection connection = ConnectionPoolCreator.getInstance().getConnection();
				PreparedStatement ps = connection.prepareStatement(selectByIDQuery.toString(),
						ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				fields.forEach((fName, field) -> {
					if (!"id".equals(fName)) {
						try {
							field.setAccessible(true);
							Object value = field.get(obj);
							field.setAccessible(false);
							rs.updateObject(fName, value);
						} catch (IllegalArgumentException | IllegalAccessException | SQLException e) {
							e.printStackTrace();
						}
					}
				});
				rs.updateRow();
				update = true;
			}
		}

		return update;
	}

}
