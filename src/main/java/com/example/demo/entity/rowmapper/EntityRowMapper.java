package com.example.demo.entity.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.example.demo.entity.Entity;

public class EntityRowMapper implements RowMapper<Entity> {

	@Override
	public Entity mapRow(ResultSet rs, int rowNum) throws SQLException {
		return new Entity(
				rs.getInt(Entity.ID),
				rs.getString(Entity.FULL_NAME),
				rs.getInt(Entity.INSERT_DATE)
				);
	}

}
