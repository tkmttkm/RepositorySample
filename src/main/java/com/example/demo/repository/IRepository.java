package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;

/**
 * <pre>
 * リポジトリクラスのインターフェース
 * 実装の際はエンティティクラスを渡す
 * </pre>
 * @author Takumi
 *
 * @param <T> エンティティクラス
 */
public interface IRepository<T> {
	
	/**
	 * 指定したidのデータを取得する
	 * @param id
	 * @return 指定したidのデータ
	 * @throws DataAccessException
	 */
	Optional<T> findById(Object id) throws DataAccessException;
	
	/**
	 * データをすべて取得する
	 * @return テーブルデータすべて
	 * @throws DataAccessException
	 */
	List<T> findAll() throws DataAccessException;
	
	/**
	 * 指定したキーのidを削除する
	 * @param id
	 * @throws DataAccessException
	 */
	void deleteById(Object id) throws DataAccessException;
	
	/**
	 * @param saveList 保存したいデータのリスト
	 * 複数データを保存する
	 * @throws Exception 
	 * @throws Exception
	 */
	void save(List<T> saveList) throws Exception;
	
	/**
	 * @param saveData 保存したいデータ
	 * データを1つ保存する
	 * @throws Exception 
	 */
	void save(T saveData) throws Exception;
}
