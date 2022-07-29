package com.sparta.springcore.controller;

import com.sparta.springcore.dto.ProductMypriceRequestDto;
import com.sparta.springcore.dto.ProductRequestDto;
import com.sparta.springcore.model.Product;
import com.sparta.springcore.model.UserRoleEnum;
import com.sparta.springcore.security.UserDetailsImpl;
import com.sparta.springcore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.management.relation.Role;
import java.util.List;

@RestController // JSON으로 데이터를 주고받음을 선언합니다.
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 신규 상품 등록
    @PostMapping("/api/products")
    public Product createProduct(@RequestBody ProductRequestDto requestDto,
                                 @AuthenticationPrincipal UserDetailsImpl userDetails) { //유저별 관심상품 출력 기능을 위한 유저Id를 받아오기 위해 추가된 부분
// 로그인 되어 있는 회원 테이블의 ID
        Long userId = userDetails.getUser().getId();

        Product product = productService.createProduct(requestDto, userId);

// 응답 보내기
        return product;
    }

    // 설정 가격 변경
    @PutMapping("/api/products/{id}") // getProducts메소드처럼 로그인한 유저의 정보가 필요없다. 로그인한 유저가 전제로 갈려있고 id는 path로 받기떄문에
    // but 로그인 한 유저가 다른사람의 상품을 볼 수도 있는 방법이 있다고 하셨기 때문에 UserDetailsImpl을 통해서 한 번 더 확인하는 작업를 거치는게 더 좋을 것 같다.
    public Long updateProduct(@PathVariable Long id, @RequestBody ProductMypriceRequestDto requestDto) {
        Product product = productService.updateProduct(id, requestDto);

// 응답 보내기 (업데이트된 상품 id)
        return product.getId();
    }

    // 로그인한 회원이 등록한 관심 상품 조회
    @GetMapping("/api/products")
    public List<Product> getProducts(@AuthenticationPrincipal UserDetailsImpl userDetails) {
// 로그인 되어 있는 회원 테이블의 ID
        Long userId = userDetails.getUser().getId();

        return productService.getProducts(userId);
    }
//관리자로써 모든 상품 조회
    @Secured(UserRoleEnum.Authority.ADMIN) //@Secured 의 권한 값은 static한 값을 줘야하기 때문에 이러한 방법 사용.
    @GetMapping("/api/admin/products")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }
}