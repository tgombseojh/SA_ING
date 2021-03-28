package com.yellowbus.project.place.search.exception;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class KakaoAPIException extends RuntimeException {

    String message;

    public KakaoAPIException(String message) {
        super(message);
    }
}
