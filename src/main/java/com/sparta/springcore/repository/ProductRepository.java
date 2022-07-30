package com.sparta.springcore.repository;

import com.sparta.springcore.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {  //엔티티명 , PK 자료형
    Page<Product> findAllByUserId(Long userId, Pageable pageable);  //원래 있는 함수가 아니면 이렇게 선언을 해줘야한다.
                                                                    //Spring JPA에서 pagable 인터페이스 제공  // List가 아닌 Page로 받아야한다.
}