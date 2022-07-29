package com.sparta.springcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.springcore.dto.KakaoUserInfoDto;
import com.sparta.springcore.model.User;
import com.sparta.springcore.model.UserRoleEnum;
import com.sparta.springcore.repository.UserRepository;
import com.sparta.springcore.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class KakaoUserService {
    private final PasswordEncoder passwordEncoder; //WebSecurityConfig의 BCryptPasswordEncoder를 DI한 것 (BCryptPasswordEncoder가 PasswordEncorder를 인터페이스로 상속받고있음)
    private final UserRepository userRepository;

    @Autowired
    public KakaoUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public void kakaoLogin(String code) throws JsonProcessingException {   //throws는 JsonNode jsonNode = objectMapper.readTree(responseBody); 이 부분에서 발생할 수 있는 에러를 호출한 곳으로 보내라는 것, 즉 호출한 곳에서도 이 에러를 처리하는 로직이 있어야함
        // 1. "인가 코드"로 "액세스 토큰" 요청    // 1,2 모든 과정들이 카카오 디벨로퍼 사이트에 올라와있는 내용들이다.
        String accessToken = getAccessToken(code);
        // 2. 토큰으로 카카오 API 호출
        KakaoUserInfoDto kakaoUserInfo = getKakaoUesrInfo(accessToken);   //나중에 중복 생성되는 애들을 멤버변수로 빼내고 빈으로 생성해서 처리하는 것 해보기
        // 3. DB에 카카오ID 정보가 없을 시 회원 가입
        User kakaoUser = registerKakaoUserIfNeeded(kakaoUserInfo);
        // 4. 강제 로그인 처리
        forceLogin(kakaoUser);

    }

    private String getAccessToken(String code) throws JsonProcessingException {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

// HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", "c5e44e37743c6cc2e3d69138d3c713b7");
        body.add("redirect_uri", "http://localhost:8080/user/kakao/callback");
        body.add("code", code);

// HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();  //서버가 서버에 요청을 보낼 때 RestTemplate 를 사용한다
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

// HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();  //
        ObjectMapper objectMapper = new ObjectMapper();  //JSON형태를 자바에서 사용하기 위해 ObjectMapper 사용
        JsonNode jsonNode = objectMapper.readTree(responseBody); //처음 스트링으로 받아온 JSON포맷을 객체형태로 변화시키는 과정
        return jsonNode.get("access_token").asText();
    }

    private KakaoUserInfoDto getKakaoUesrInfo(String accessToken) throws JsonProcessingException {
        // 2. 토큰으로 카카오 API 호출
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

// HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody(); //JSON형태로 오는 응답에서 원하는 정보를 빼내는 과정
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties")
                .get("nickname").asText();
        String email = jsonNode.get("kakao_account")
                .get("email").asText();

        System.out.println("카카오 사용자 정보: " + id + ", " + nickname + ", " + email);
        KakaoUserInfoDto kakaoUserInfoDto = new KakaoUserInfoDto(id, nickname, email);
        return kakaoUserInfoDto;
    }

    private User registerKakaoUserIfNeeded(KakaoUserInfoDto kakaoUserInfo) {
        // DB 에 중복된 Kakao Id 가 있는지 확인
        Long kakaoId = kakaoUserInfo.getId();
        User kakaoUser = userRepository.findByKakaoId(kakaoId)
                .orElse(null);
        if (kakaoUser == null) {
// 회원가입
// username: kakao nickname
            String nickname = kakaoUserInfo.getNickname();

// password: random UUID
            String password = UUID.randomUUID().toString(); // 랜덤 평문
            String encodedPassword = passwordEncoder.encode(password); //평문이기때문에 인코딩

// email: kakao email
            String email = kakaoUserInfo.getEmail();
// role: 일반 사용자
            UserRoleEnum role = UserRoleEnum.USER;

            kakaoUser = new User(nickname, encodedPassword, email, role, kakaoId);
            userRepository.save(kakaoUser);
        }
        return kakaoUser;
    }

    private void forceLogin(User kakaoUser) {
        UserDetails userDetails = new UserDetailsImpl(kakaoUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}