package com.example.demo.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Entity;
import com.example.demo.repository.extend.Repository;

@SpringBootTest
@Transactional
class TestRepository {

	@Autowired
	private Repository repository;

	@Test
	void testFindAll() {
		List<Entity> dataList = repository.findAll();
		assertEquals(repository.getFindCount(), 4);

		//本来は全ゼータチェックするべきだが、ここではidが２のデータのみ確認
		Entity data_Id2 = dataList.stream().filter(data -> Integer.valueOf(data.getId()) == 2).toList().get(0);
		assertEquals(data_Id2.getFull_name(), "エンティティ　ティティ");
		assertEquals(data_Id2.getInsert_date(), 20240418);
	}

	@Test
	void testFindById() {
		Optional<Entity> dataOpt = repository.findById(1);

		if (dataOpt.isPresent()) {
			Entity data = dataOpt.get();
			assertEquals(repository.getFindCount(), 1);
			assertEquals(data.getFull_name(), "エンティティ　ダンプティ");
			assertEquals(data.getInsert_date(), 20240418);
		} else {
			assertEquals(repository.getFindCount(), 0);
		}
	}

	@Test
	void testDeleteById() {
		repository.deleteById(1);
		assertEquals(repository.getDeleteCount(), 1);
		
		Optional<Entity> dataId1Opt = repository.findById(1);
		//データがないことで削除確認
		assertTrue(dataId1Opt.isEmpty());
	}

	@Test
	void testSave() throws Exception {
		repository.save(new Entity(2, "テスト ダンプティ", 20240507));
		repository.save(new Entity(8, "テスト", 20240506));
		
		
		Optional<Entity> data_Id8Opt = repository.findById(8);
		Entity data_Id8 = data_Id8Opt.isPresent() ? data_Id8Opt.get() : null;
		
		Optional<Entity> data_Id2Opt = repository.findById(2);
		Entity data_Id2 = data_Id2Opt.isPresent() ? data_Id2Opt.get() : null;
		
		//データがなければエラー
		if (data_Id2 == null || data_Id8 == null) {
			fail("バグっとる");
		}
		
		assertEquals(data_Id2.getFull_name(), "テスト ダンプティ");
		assertEquals(data_Id2.getInsert_date(), 20240507);

		assertEquals(data_Id8.getFull_name(), "テスト");
		assertEquals(data_Id8.getInsert_date(), 20240506);
	}
	
	@Test
	void testBatchSave() throws Exception {
		List<Entity> saveList = new ArrayList<>();
		
		Entity entity1 = new Entity(2, "テスト ダンプティ", 20240507);
		Entity entity2 = new Entity(8, "テスト", 20240506);
		
		saveList.add(entity1);
		saveList.add(entity2);
		
		repository.save(saveList);
		
		Optional<Entity> data_Id8Opt = repository.findById(8);
		Entity data_Id8 = data_Id8Opt.isPresent() ? data_Id8Opt.get() : null;
		
		Optional<Entity> data_Id2Opt = repository.findById(2);
		Entity data_Id2 = data_Id2Opt.isPresent() ? data_Id2Opt.get() : null;
		
		if (data_Id2 == null || data_Id8 == null) {
			fail("バグっとる");
		}
		
		assertEquals(data_Id2.getFull_name(), "テスト ダンプティ");
		assertEquals(data_Id2.getInsert_date(), 20240507);

		assertEquals(data_Id8.getFull_name(), "テスト");
		assertEquals(data_Id8.getInsert_date(), 20240506);
	}
}
