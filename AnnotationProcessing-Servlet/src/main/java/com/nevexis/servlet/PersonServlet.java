package com.nevexis.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nevexis.model.Person;
import com.nevexis.model.dao.MyEntityManager;

@WebServlet("/person")
public class PersonServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	Logger log = Logger.getLogger(this.getClass().getSimpleName());

	Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private final String RESPONSE_JSON_MESSAGE = "{ \"message\":\"%s\" }";

	// GET
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		List<Person> people = new ArrayList<>();

		try {
			people = MyEntityManager.select(Person.class);
		} catch (SQLException e) {
			log.warning("Error in manipulating with DB! SELECT METHOD");
			e.printStackTrace();
		}

		String peopleJSON = gson.toJson(people);

		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		out.print(peopleJSON);
		out.flush();
	}

	// INSERT
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		StringBuilder sb = new StringBuilder();

		try (BufferedReader bufferReader = request.getReader()) {
			String line = null;
			while (Objects.nonNull(line = bufferReader.readLine())) {
				sb.append(line);
			}
		}

		Person person = gson.fromJson(sb.toString(), Person.class);
		boolean inserted = false;
		try {
			inserted = MyEntityManager.insert(person);
		} catch (SQLException e) {
			log.warning("Error in manipulating with DB! INSERT METHOD");
			e.printStackTrace();
		}

		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String message = inserted ? "OK" : "Person isn't inserted";
		out.print(messageResponseJSON(message));
		out.flush();
	}

	// UPDATE
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		StringBuilder sb = new StringBuilder();

		try (BufferedReader bufferReader = request.getReader()) {
			String line = null;
			while (Objects.nonNull(line = bufferReader.readLine())) {
				sb.append(line);
			}
		}

		Person person = gson.fromJson(sb.toString(), Person.class);
		boolean update = false;
		try {
			update = MyEntityManager.update(person);
		} catch (SQLException e) {
			log.warning("Error in manipulating with DB! UPDATE METHOD");
			e.printStackTrace();
		}

		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String message = update ? "OK" : "Person isn't updated";
		out.print(messageResponseJSON(message));
		out.flush();
	}

	private String messageResponseJSON(String message) {
		return String.format(RESPONSE_JSON_MESSAGE, message);
	}

}
