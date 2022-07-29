package com.sparta.springcore.security;

import com.sparta.springcore.model.User;
import com.sparta.springcore.model.UserRoleEnum;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class UserDetailsImpl implements UserDetails {  //UserDetailsService 가  DB에서 가져온 데이터를 담는 곳  //인터페이스의 필수조건 살펴보기

    private final User user; //로그인 된 사용자

    public UserDetailsImpl(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {  //정해놓은 룰을 따라야하므로 하나하나 이해하려고 하지말자.
        UserRoleEnum role = user.getRole();
        String authority = role.getAuthority();  //"ROLE_USER" or "ROLE_ADMIN" 둘 중 하나 반환

        SimpleGrantedAuthority simpleGrantedAuthority =  new SimpleGrantedAuthority(authority); //GrantedAuthority 가 상속받는 인터페이스
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(simpleGrantedAuthority);

        return authorities;
    }
}