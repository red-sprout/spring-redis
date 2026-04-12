package org.example.springredis.repository;

import org.example.springredis.model.Product;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * CrudRepository 를 상속하면 기본 CRUD 가 자동 구현됩니다.
 *
 * 제공되는 기본 메서드:
 *   save(entity)          → HSET product:{id} ...
 *   findById(id)          → HGETALL product:{id}
 *   existsById(id)        → EXISTS product:{id}
 *   findAll()             → 전체 id Set 순회 후 HGETALL
 *   count()               → SCARD product
 *   deleteById(id)        → DEL product:{id} + 인덱스 정리
 *   deleteAll()           → 전체 삭제
 */
public interface ProductRepository extends CrudRepository<Product, String> {

    // @Indexed 선언된 name 필드에 대해 보조 인덱스로 조회
    // → product:name:{name} Set 에서 id 목록을 꺼낸 후 각각 HGETALL
    List<Product> findByName(String name);
}
