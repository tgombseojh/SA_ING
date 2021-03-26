package com.yellowbus.project.place.search.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Setter @Getter
public class ApiException extends RuntimeException {

    String message;
    HttpStatus httpStatus;

}
