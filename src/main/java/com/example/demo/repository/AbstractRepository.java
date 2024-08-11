package com.example.demo.repository;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.example.demo.entity.Entity;

import lombok.RequiredArgsConstructor;

/**
 * <pre>
 * リポジトリの抽象クラス
 * コンストラクタには{@code <S>}のオブジェクトを生成したクラスを引数に渡す
 * </pre>
 * @author Takumi
 * @param <T> エンティティクラス
 * @param <S> {@link RowMapper}を実装したクラス
 */
@RequiredArgsConstructor
public abstract class AbstractRepository<T, S> implements IRepository<T> {

	@Autowired
	protected NamedParameterJdbcTemplate namedjdbcTemplate;

	/** {@link #findAll()}や{@link #findById(Object)}で取得したデータを格納 */
	private List<T> dataList;

	/** エンティティクラス */
	protected final T entity;
	/** <pre>
	 * {@link RowMapper}を実装したクラスを型に持つ
	 * {@link #findAll()}や{@link #findById(Object)}のマッピングで使用
	 * </pre> */
	protected final S rowMapper;


	/** {@link #findAll()}や{@link #findById(Object)}で取得したデータ数 */
	@SuppressWarnings("unused")
	private int findCount;

	/** {@link #save(List)}や{@link #save(Object)}で保存したデータ数 */
	private int saveCount;

	/** {@link #deleteById(Object)}で削除したデータ数 */
	private int deleteCount;

	public int getFindCount() {
		return dataList.size();
	}

	public int getSaveCount() {
		return saveCount;
	}

	public void setSaveCount(int saveCount) {
		this.saveCount = saveCount;
	}

	public int getDeleteCount() {
		return deleteCount;
	}

	public void setDeleteCount(int deleteCount) {
		this.deleteCount = deleteCount;
	}

	public List<T> getDataList() {
		return dataList;
	}

	public void setDataList(List<T> dataList) {
		this.dataList = dataList;
	}
	/**
	 * エンティティのフィールド名を{@code List<String>}にして返す
	 * @return フィールド名をつめた{@code List<String>}
	 * @throws Exception
	 */
	protected List<String> getFieldNames() throws Exception {
		try {
			List<String> list = new ArrayList<>();
			Field[] fields = entity.getClass().getDeclaredFields();
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
	 * エンティティのstaticフィールド以外のフィールド名とその値を{@code Map<String, Object>}として返す
	 * @param entity
	 * @return key:フィールド名 value:そのフィールドの値 の{@code Map<String, Object>}
	 * @throws Exception
	 */
	protected Map<String, Object> getFieldNameAndValue(Entity entity) throws Exception {
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
	 * <pre>
	 * key名 = VALUES(:key名), key名 = VALUES(:key名), .....
	 * を作成する
	 * </pre>
	 * @param sql 作成中のSQL文
	 * @param fieldNameAndValue key:カラム名 value:更新したい値
	 */
	protected void setDuplicateUpdateSqlByMap(StringBuilder sql, Map<String, Object> fieldNameAndValue) {
		List<String> updateSqlList = new ArrayList<>();
		for (var entrySet : fieldNameAndValue.entrySet()) {
			updateSqlList.add(entrySet.getKey() + " = VALUES(" + entrySet.getKey() + ")");
		}
		sql.append(String.join(", ", updateSqlList));
	}

	/**
	 * <pre>
	 * key名 = VALUES(:key名), key名 = VALUES(:key名), .....
	 * を作成する
	 * </pre>
	 * @param sql 作成中のSQL文
	 * @param fieldNameAndValue key:カラム名 value:更新したい値
	 * @throws Exception
	 */
	protected void setDuplicateUpdateSqlByMap(StringBuilder sql) throws Exception {
		List<String> updateSqlList = new ArrayList<>();
		for (String fieldName : getFieldNames()) {
			updateSqlList.add(fieldName + " = VALUES(" + fieldName + ")");
		}
		sql.append(String.join(", ", updateSqlList) + " ");
	}

	/**
	 * <pre>
	 * (key名, key名, key名, ...) VALUES (:key名, :key名, .....)
	 * を作成する
	 * </pre>
	 * @param sql 作成中のSQL文
	 * @param fieldNameAndValue key:カラム名 value:その値 のMap<String, Object> {@link #getFieldNameAndValue(Entity)}を使用して作成
	 * @param params 作成中のパラメター
	 */
	protected void setInsertSqlByMap(StringBuilder sql, Map<String, Object> fieldNameAndValue,
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

		sql.append("( " + String.join(", ", columnArray) + " ) VALUES ( " + String.join(", ", valueArray) + " ) ");
	}

	/**
	 * <pre>
	 * (key名, key名, key名, ...) VALUES (:key名, :key名, .....)
	 * を作成する
	 * </pre>
	 * @param sql 作成中のSQL文
	 * @throws Exception
	 */
	protected void setInsertSqlByMap(StringBuilder sql) throws Exception {
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

		sql.append("( " + String.join(", ", columnArray) + " ) VALUES ( " + String.join(", ", valueArray) + " ) ");
	}

}
