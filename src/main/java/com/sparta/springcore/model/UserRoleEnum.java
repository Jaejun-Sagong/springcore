package com.sparta.springcore.model;

public enum UserRoleEnum {    //enum은 클래스처럼 보이게 하는 상수가 특징이다.
    USER(Authority.USER), // 사용자 권한    //그냥 USER,ADMIN만 선언하고 ProductController에서 변수명을 그대로 갖다 써도 되지만 그럼 나중에 변수명이 변경되거나 추가됐을 때 불편한 점 존재.
                                           //UserRoleEnum의 변화가 Controller에 최대한 영향을 안주도록 메소드를 사용해서 넘기는 방식 사용
                                           //소괄호가 나오면 생성자를 호출한다. -> enum의 생성자는 열거 상수 옆에 붙는 형식으로 나온다.
    ADMIN(Authority.ADMIN); // 관리자 권한

    private final String authority;  //필드값을 추가하면 열거형 상수가 객체의 성질을 띄기때문에 멤버변수처럼 사용할 수 있다.

    UserRoleEnum(String authority){    //열거형 상수 우측에 소괄호()가 있는 순간 이 생성자가 호출되며 ()내에 적혀있는 것을 매개변수로 받아온다.
        this.authority = authority;
    }
    public String getAuthority(){
        return this.authority;
    }

    public static class Authority{
        public static final String USER = "ROLE_USER";
        public static final String ADMIN = "ROLE_ADMIN";
    }
}