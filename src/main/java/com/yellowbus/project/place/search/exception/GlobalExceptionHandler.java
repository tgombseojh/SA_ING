package com.yellowbus.project.place.search.exception;

import javassist.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@RestControllerAdvice
@EnableWebMvc
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ExceptionInfo> noHandlerFoundException(NoHandlerFoundException e) {
        ExceptionInfo message = new ExceptionInfo("SE01", "Incorrect query request (잘못된 쿼리요청)");
        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionInfo> notFoundException(NotFoundException e) {
        ExceptionInfo message = new ExceptionInfo("SE02", "Resource not found (반환 데이터 없음)");
        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(KakaoAPIException.class)
    public ResponseEntity<ExceptionInfo> kakaoException(KakaoAPIException e) { // 오픈 API 에러 감지
        ExceptionInfo message = new ExceptionInfo("SE04", "Kakao Open API 에러, "+e.getMessage());
        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NaverAPIException.class)
    public ResponseEntity<ExceptionInfo> naverException(NaverAPIException e) { // 오픈 API 에러 감지
        ExceptionInfo message = new ExceptionInfo("SE05", "Naver Open API 에러, "+e.getMessage());
        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SignupException.class)
    public ResponseEntity<ExceptionInfo> signUpException(SignupException e) { // MemberController signup 에러 감지
        ExceptionInfo message = new ExceptionInfo("SE06", "User exist (다른 아이디를 사용해주세요)");
        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
