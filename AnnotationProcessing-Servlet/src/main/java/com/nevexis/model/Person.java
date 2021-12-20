package com.nevexis.model;

import com.nevexis.model.annotations.Entity;
import com.nevexis.model.annotations.Id;
import com.nevexis.model.annotations.Property;

@Entity("people")
public class Person {

	@Id
	private Long id;

	@Property("name")
	private String name;

	@Property("age")
	private Integer age;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Integer getAge() {
		return age;
	}

	public Person setId(Long id) {
		this.id = id;
		return this;
	}

	public Person setName(String name) {
		this.name = name;
		return this;
	}

	public Person setAge(Integer age) {
		this.age = age;
		return this;
	}

}
