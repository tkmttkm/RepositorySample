package com.example.demo.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

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

}
