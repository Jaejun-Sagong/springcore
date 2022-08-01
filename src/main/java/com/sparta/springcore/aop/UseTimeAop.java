package com.sparta.springcore.aop;

import com.sparta.springcore.model.ApiUseTime;
import com.sparta.springcore.model.User;
import com.sparta.springcore.repository.ApiUseTimeRepository;
import com.sparta.springcore.security.UserDetailsImpl;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class UseTimeAop {
    private final ApiUseTimeRepository apiUseTimeRepository;

    public UseTimeAop(ApiUseTimeRepository apiUseTimeRepository) {
        this.apiUseTimeRepository = apiUseTimeRepository;
    }
    //execution(modifiers-pattern? return-type-pattern declaring-type-pattern? method-name-pattern(param-pattern) throws-pattern?)
    @Around("execution(public * com.sparta.springcore.controller..*(..))") //public에만 적용 // * -> 리턴타입은 무관하게 다 적용하겠다. //controller패키지에 적용하겠다. //..은 controller 아래 패키지와 클래스를 모두 포함 // *-> 모든 함수에 적용 //(..) -> 파라미터 인수 개수와 타입 상관없음
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {  // return타입은
// 측정 시작 시간
        long startTime = System.currentTimeMillis();

        try {
// 핵심기능 수행
            Object output = joinPoint.proceed(); //Controller 수행부분
            return output; //클라이언트로 Response
        } finally {
// 측정 종료 시간
            long endTime = System.currentTimeMillis();
// 수행시간 = 종료 시간 - 시작 시간
            long runTime = endTime - startTime;

// 로그인 회원이 없는 경우, 수행시간 기록하지 않음
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal().getClass() == UserDetailsImpl.class) {
// 로그인 회원 정보
                UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
                User loginUser = userDetails.getUser();

// API 사용시간 및 DB 에 기록
                ApiUseTime apiUseTime = apiUseTimeRepository.findByUser(loginUser)
                        .orElse(null);
                if (apiUseTime == null) {
// 로그인 회원의 기록이 없으면
                    apiUseTime = new ApiUseTime(loginUser, runTime);
                } else {
// 로그인 회원의 기록이 이미 있으면
                    apiUseTime.addUseTime(runTime);
                }

                System.out.println("[API Use Time] Username: " + loginUser.getUsername() + ", Total Time: " + apiUseTime.getTotalTime() + " ms");
                apiUseTimeRepository.save(apiUseTime);
            }
        }
    }
}
