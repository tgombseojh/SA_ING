package com.yellowbus.project.place.search.exception;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class NaverAPIException extends RuntimeException {

    String message;

    public NaverAPIException(String message) {
        super(message);
    }
}
