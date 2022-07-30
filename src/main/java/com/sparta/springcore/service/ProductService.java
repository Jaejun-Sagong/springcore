package com.sparta.springcore.service;

import com.sparta.springcore.dto.ProductMypriceRequestDto;
import com.sparta.springcore.dto.ProductRequestDto;
import com.sparta.springcore.model.Folder;
import com.sparta.springcore.model.Product;
import com.sparta.springcore.model.User;
import com.sparta.springcore.repository.FolderRepository;
import com.sparta.springcore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final FolderRepository folderRepository;
    public static final int MIN_MY_PRICE = 100;

    @Autowired
    public ProductService(ProductRepository productRepository, FolderRepository folderRepository) {
        this.productRepository = productRepository;
        this.folderRepository = folderRepository;
    }

    public Product createProduct(ProductRequestDto requestDto, Long userId) {
// 요청받은 DTO 로 DB에 저장할 객체 만들기
        Product product = new Product(requestDto, userId);

        productRepository.save(product);

        return product;
    }

    public Product updateProduct(Long id, ProductMypriceRequestDto requestDto) {
        int myprice = requestDto.getMyprice();
        if (myprice < MIN_MY_PRICE) {
            throw new IllegalArgumentException("유효하지 않은 관심 가격입니다. 최소 " + MIN_MY_PRICE + " 원 이상으로 설정해 주세요.");
        } // throw가 실행되면 아래 구문은 실행되지않고 해당 에러를 return해준다.

        Product product = productRepository.findById(id)  //updateProduct를 test할 때 이 부분에서 에러 안나게 조심
                .orElseThrow(() -> new NullPointerException("해당 아이디가 존재하지 않습니다."));

        product.setMyprice(myprice); //이 부분도 에러가 날 가능성이 있다. => MiN_MY_PRICE만 테스트하고싶은데 디펜던시가 강해서 불편함
        productRepository.save(product);  //의존성을 낮추기 위해 가짜 객체라는 개념 존재

        return product;
    }

    // 회원 ID 로 등록된 상품 조회
    public Page<Product> getProducts(Long userId, int page, int size, String sortBy, boolean isAsc) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;   //Sort.Direction도 지원을 해주는 기능이다.
        Sort sort = Sort.by(direction, sortBy); //Spring Framwork에서 Sort라는 클래스를 지원해준다.
        Pageable pageable = PageRequest.of(page, size, sort);  //PageRequest는 pageable 인터페이스의 구현체이기 때문에 이렇게 사용 가능

        return productRepository.findAllByUserId(userId, pageable);
    }

    // (관리자용) 상품 전체 조회
    public Page<Product> getAllProducts(int page, int size, String sortBy, boolean isAsc) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;   //Sort.Direction도 지원을 해주는 기능이다.
        Sort sort = Sort.by(direction, sortBy); //Spring Framwork에서 Sort라는 클래스를 지원해준다.
        Pageable pageable = PageRequest.of(page, size, sort);  //PageRequest는 pageable 인터페이스의 구현체이기 때문에 이렇게 사용 가능
        return productRepository.findAll(pageable);  //productRepository의 상세정보를 들어가보면 finaAll의 매개변수로 pagealbe을 받을 때에는 Page로 반환해야한다라고 적혀있다.
    }

    @Transactional
    public Product addFolder(Long productId, Long folderId, User user) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NullPointerException("해당 상품 아이디가 존재하지 않습니다."));

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new NullPointerException("해당 폴터 아이디가 존재하지 않습니다."));

        Long loginUserId = user.getId();   // 이 부분은 없어도 잘 동작하겠지만 혹시 모를 보안문제떄문에 넣었다.
        if (!product.getUserId().equals(loginUserId) || !folder.getUser().getId().equals(loginUserId)) {
            throw new IllegalArgumentException("회원님의 관심상품이 아니거나, 회원님의 폴더가 아닙니다.");
        }

        product.addFolder(folder);
        return product;
    }
}