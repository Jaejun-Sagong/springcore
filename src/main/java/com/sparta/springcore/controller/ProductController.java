package com.sparta.springcore.controller;

import com.sparta.springcore.model.Product;
import com.sparta.springcore.service.ProductService;
import com.sparta.springcore.dto.ProductMypriceRequestDto;
import com.sparta.springcore.dto.ProductRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

//@RequiredArgsConstructor // final로 선언된 멤버 변수를 자동으로 생성합니다.
@RestController // JSON으로 데이터를 주고받음을 선언합니다.
public class ProductController {

    private final ProductService productService;

    @Autowired //이 생성자를 통해 필드에 있는 productService를 마치 DI처럼 사용 가능한 것. 이 생성자를 대체해주는게 @RequiredArgsConstructor이지만 학습차원에서 직접 적어줌
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
//    @Autowired     //ApplicationContext를 이용해서 수동으로 Bean 꺼내오는 방법
//    public ProductService(ApplicationContext context) {
//        // 1.'빈' 이름으로 가져오기
//        ProductRepository productRepository = (ProductRepository) context.getBean("productRepository");
//        // 2.'빈' 클래스 형식으로 가져오기
//        // ProductRepository productRepository = context.getBean(ProductRepository.class);
//        this.productRepository = productRepository;
//    }
    // 신규 상품 등록
    @PostMapping("/api/products")
    public Product createProduct(@RequestBody ProductRequestDto requestDto) throws SQLException {
        Product product = productService.createProduct(requestDto);

        // 응답 보내기
        return product;
    }

    // 설정 가격 변경
    @PutMapping("/api/products/{id}")
    public Long updateProduct(@PathVariable Long id, @RequestBody ProductMypriceRequestDto requestDto) throws SQLException {
        Product product = productService.updateProduct(id, requestDto);

        // 응답 보내기 (업데이트된 상품 id)
        return product.getId();
    }

    // 등록된 전체 상품 목록 조회
    @GetMapping("/api/products")
    public List<Product> getProducts() throws SQLException {
        List<Product> products = productService.getProducts();

        // 응답 보내기
        return products;
    }
}