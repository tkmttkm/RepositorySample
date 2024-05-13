package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Entity {

	private Integer id;
	private String full_name;
	private Integer insert_date;
	
	public static String TABLE_NAME = "ENTITY_TEST_TABLE";
	
	public static String ID = "id";
	public static String FULL_NAME = "full_name";
	public static String INSERT_DATE = "insert_date";
	
}
