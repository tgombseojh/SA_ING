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

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ExceptionInfo> nullPointerException(NullPointerException e) {
        ExceptionInfo message = new ExceptionInfo("SE03", "server error (서버 오류)");
        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ExceptionInfo> apiException(ApiException e) { // 오픈 API 에러 감지에 사용
        ExceptionInfo message = new ExceptionInfo("SE04", "Open api server error (오픈 api 서버 오류)");
        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
