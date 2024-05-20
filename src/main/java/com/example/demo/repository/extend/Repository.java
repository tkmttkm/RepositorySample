package com.example.demo.repository.extend;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
 * {@link Entity}クラスとマッピングしている
 * DIコンテナに登録するため必ず{@link org.springframework.stereotype.Repository}を付与
 * </pre>
 * @author Takumi
 *
 */
@org.springframework.stereotype.Repository
public class Repository extends AbstractRepository<Entity, EntityRowMapper> {

	public Repository() {
		super(new EntityRowMapper());
	}

	@Override
	public List<Entity> findAll() throws DataAccessException {
		List<String> select = new ArrayList<>();

		select.add("SELECT");
		select.add("*");
		select.add("FROM");
		select.add(Entity.TABLE_NAME);

		try {
			setDataList(namedjdbcTemplate.query(String.join(" ", select), new EmptySqlParameterSource(), rowMapper));
			return getDataList();
		} catch (DataAccessException e) {
			System.err.println(e.getMessage() + "\r\n" + e.getStackTrace());
			throw e;
		}
	}

	@Override
	public Optional<Entity> findById(Object id) throws DataAccessException {
		List<String> select = new ArrayList<>();

		select.add("SELECT");
		select.add("*");
		select.add("FROM");
		select.add(Entity.TABLE_NAME);
		select.add("WHERE");
		select.add(Entity.ID + " = :" + Entity.ID);

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue(Entity.ID, id);

		try {
			setDataList(namedjdbcTemplate.query(String.join(" ", select), params, rowMapper));

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
		List<String> delete = new ArrayList<>();

		delete.add("DELETE");
		delete.add("FROM");
		delete.add(Entity.TABLE_NAME);
		delete.add("WHERE");
		delete.add(Entity.ID + " = :" + Entity.ID);

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue(Entity.ID, id);

		try {
			setDeleteCount(namedjdbcTemplate.update(String.join(" ", delete), params));
		} catch (DataAccessException e) {
			System.err.println(e.getMessage() + "\r\n" + e.getStackTrace());
			throw e;
		}
	}

	@Override
	public void save(List<Entity> saveList) throws Exception {
		SqlParameterSource[] batchParam = SqlParameterSourceUtils.createBatch(saveList);
		List<String> save = new ArrayList<>();
		try {

			save.add("INSERT INTO");
			save.add(Entity.TABLE_NAME);
			setInsertSqlByMap(save);

			save.add("ON DUPLICATE KEY");
			save.add("UPDATE");
			setDuplicateUpdateSqlByMap(save);
			save.add(";");

			int[] saveCounts = namedjdbcTemplate.batchUpdate(String.join(" ", save), batchParam);
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
			List<String> save = new ArrayList<>();
			//データがなければインサート、あればアップデートをかける
			save.add("INSERT INTO");
			save.add(Entity.TABLE_NAME);
			setInsertSqlByMap(save, fieldNameAndValue, params);
			save.add("ON DUPLICATE KEY");
			save.add("UPDATE");
			setDuplicateUpdateSqlByMap(save, fieldNameAndValue);
			save.add(";");

			String sql = String.join(" ", save);

			setSaveCount(namedjdbcTemplate.update(sql, params));
		} catch (Exception e) {
			System.err.println(e.getMessage() + "\r\n" + e.getStackTrace());
			throw e;
		}
	}

	/**
	 * エンティティのstaticフィールド以外のフィールド名とその値を{@code Map<String, Object>}として返す
	 * @param entity
	 * @return key:フィールド名 value:そのフィールドの値 の{@code Map<String, Object>}
	 * @throws Exception
	 */
	private Map<String, Object> getFieldNameAndValue(Entity entity) throws Exception {
		try {
			Map<String, Object> map = new LinkedHashMap<>();

			Field[] fields = entity.getClass().getDeclaredFields();
			for (Field field : fields) {
				//privateフィールドへのアクセスを可能にする
				field.setAccessible(true);
				//staticフィールドの場合はスキップする
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}

				String name = field.getName();
				Object value = field.get(entity);
				map.put(name, value);
			}

			return map;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			System.err.println(e.getMessage() + "\r\n" + e.getStackTrace());
			throw e;
		}
	}

	/**
	 * エンティティのフィールド名を{@code List<String>}にして返す
	 * @return フィールド名をつめた{@code List<String>}
	 * @throws Exception
	 */
	private List<String> getFieldNames() throws Exception {
		try {
			List<String> list = new ArrayList<>();

			Field[] fields = new Entity().getClass().getDeclaredFields();
			for (Field field : fields) {
				//privateフィールドへのアクセスを可能にする
				field.setAccessible(true);
				// staticフィールドの場合はスキップする
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				list.add(field.getName());
			}

			return list;
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage() + "\r\n" + e.getStackTrace());
			throw e;
		}
	}

	/**
	 * <pre>
	 * key名 = VALUES(:key名), key名 = VALUES(:key名), .....
	 * を作成する
	 * </pre>
	 * @param sqlList 作成中のSQL文
	 * @param fieldNameAndValue key:カラム名 value:更新したい値
	 */
	private void setDuplicateUpdateSqlByMap(List<String> sqlList, Map<String, Object> fieldNameAndValue) {
		List<String> updateSqlList = new ArrayList<>();
		for (var entrySet : fieldNameAndValue.entrySet()) {
			updateSqlList.add(entrySet.getKey() + " = VALUES(" + entrySet.getKey() + ")");
		}
		sqlList.add(String.join(", ", updateSqlList));
	}

	private void setDuplicateUpdateSqlByMap(List<String> sqlList) throws Exception {
		List<String> updateSqlList = new ArrayList<>();
		try {
			for (String fieldName : getFieldNames()) {
				updateSqlList.add(fieldName + " = VALUES(" + fieldName + ")");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		sqlList.add(String.join(", ", updateSqlList));
	}

	/**
	 * <pre>
	 * (key名, key名, key名, ...) VALUES (:key名, :key名, .....)
	 * を作成する
	 * </pre>
	 * @param sqlList 作成中のSQL文
	 * @param fieldNameAndValue key:カラム名 value:その値 のMap<String, Object> {@link #getFieldNameAndValue(Entity)}を使用して作成
	 * @param params 作成中のパラメター
	 */
	private void setInsertSqlByMap(List<String> sqlList, Map<String, Object> fieldNameAndValue,
			MapSqlParameterSource params) {
		String[] columnArray = new String[fieldNameAndValue.size()];
		String[] valueArray = new String[fieldNameAndValue.size()];

		int index = 0;
		for (var entrySet : fieldNameAndValue.entrySet()) {
			String setKey = entrySet.getKey();
			int idx = 0;
			while (params.hasValue(setKey)) {
				setKey = setKey + Integer.valueOf(idx).toString();
				idx++;
			}
			params.addValue(setKey, entrySet.getValue());
			columnArray[index] = entrySet.getKey();
			valueArray[index] = ":" + setKey;
			index++;
		}

		sqlList.add("( " + String.join(", ", columnArray) + " ) VALUES ( " + String.join(", ", valueArray) + " )");
	}

	/**
	 * <pre>
	 * (key名, key名, key名, ...) VALUES (:key名, :key名, .....)
	 * を作成する
	 * </pre>
	 * @param sqlList 作成中のSQL文
	 * @throws Exception
	 */
	private void setInsertSqlByMap(List<String> sqlList) throws Exception {
		List<String> fieldNames;
		try {
			fieldNames = getFieldNames();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		String[] columnArray = new String[fieldNames.size()];
		String[] valueArray = new String[fieldNames.size()];

		int index = 0;
		for (String fieldName : fieldNames) {
			columnArray[index] = fieldName;
			valueArray[index] = ":" + fieldName;
			index++;
		}

		sqlList.add("( " + String.join(", ", columnArray) + " ) VALUES ( " + String.join(", ", valueArray) + " )");
	}

}
