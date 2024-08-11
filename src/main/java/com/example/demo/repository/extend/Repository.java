package com.example.demo.repository.extend;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;

import com.example.demo.entity.Entity;
import com.example.demo.entity.rowmapper.EntityRowMapper;
import com.example.demo.repository.AbstractRepository;

/**
 * <pre>
 * リポジトリクラス
 * {@code @Autowired}で使用
 * </pre>
 * @author Takumi
 *
 */
@org.springframework.stereotype.Repository
public class Repository extends AbstractRepository<Entity, EntityRowMapper> {

	public Repository() {
		super(new Entity(), new EntityRowMapper());
	}

	@Override
	public List<Entity> findAll() throws DataAccessException {
		StringBuilder select = new StringBuilder();

		select.append("SELECT ");
		select.append("* ");
		select.append("FROM ");
		select.append(Entity.TABLE_NAME);

		try {
			setDataList(namedjdbcTemplate.query(select.toString(), new EmptySqlParameterSource(), rowMapper));
			return getDataList();
		} catch (DataAccessException e) {
			System.err.println(e.getMessage() + "\r\n" + e.getStackTrace());
			throw e;
		}
	}

	@Override
	public Optional<Entity> findById(Object id) throws DataAccessException {
		StringBuilder select = new StringBuilder();

		select.append("SELECT ");
		select.append("* ");
		select.append("FROM ");
		select.append(Entity.TABLE_NAME);
		select.append(" WHERE ");
		select.append(Entity.ID
				+ " = :" + Entity.ID);

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue(Entity.ID, id);

		try {
			setDataList(namedjdbcTemplate.query(select.toString(), params, rowMapper));

			if (getFindCount() == 0) {
				return Optional.ofNullable(null);
			} else {
				//データは1つであることが前提
				return Optional.ofNullable(getDataList().get(0));
			}
		} catch (DataAccessException e) {
			System.err.println(e.getMessage() + "\r\n" + e.getStackTrace());
			throw e;
		}
	}

	@Override
	public void deleteById(Object id) throws DataAccessException {
		StringBuilder delete = new StringBuilder();

		delete.append("DELETE ");
		delete.append("FROM ");
		delete.append(Entity.TABLE_NAME);
		delete.append(" WHERE ");
		delete.append(Entity.ID + " = :" + Entity.ID);

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue(Entity.ID, id);

		try {
			setDeleteCount(namedjdbcTemplate.update(delete.toString(), params));
		} catch (DataAccessException e) {
			System.err.println(e.getMessage() + "\r\n" + e.getStackTrace());
			throw e;
		}
	}

	@Override
	public void save(List<Entity> saveList) throws Exception {
		SqlParameterSource[] batchParam = SqlParameterSourceUtils.createBatch(saveList);
		StringBuilder save = new StringBuilder();
		try {

			save.append("INSERT INTO ");
			save.append(Entity.TABLE_NAME + " ");
			setInsertSqlByMap(save);

			save.append("ON DUPLICATE KEY ");
			save.append("UPDATE ");
			setDuplicateUpdateSqlByMap(save);
			save.append(";");

			int[] saveCounts = namedjdbcTemplate.batchUpdate(save.toString(), batchParam);
			int saveCount = 0;
			for (int count : saveCounts) {
				saveCount += count;
			}
			setSaveCount(saveCount);

		} catch (Exception e) {
			System.err.println(e.getMessage() + "\r\n" + e.getStackTrace());
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void save(Entity saveData) throws Exception {
		Map<String, Object> fieldNameAndValue = getFieldNameAndValue(saveData);
		MapSqlParameterSource params = new MapSqlParameterSource();

		try {
			StringBuilder save = new StringBuilder();
			//データがなければインサート、あればアップデートをかける
			save.append("INSERT INTO ");
			save.append(Entity.TABLE_NAME + " ");
			setInsertSqlByMap(save, fieldNameAndValue, params);
			save.append("ON DUPLICATE KEY ");
			save.append("UPDATE ");
			setDuplicateUpdateSqlByMap(save, fieldNameAndValue);
			save.append(";");

			setSaveCount(namedjdbcTemplate.update(save.toString(), params));
		} catch (Exception e) {
			System.err.println(e.getMessage() + "\r\n" + e.getStackTrace());
			throw e;
		}
	}

}
