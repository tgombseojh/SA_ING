package com.yellowbus.project.place.search.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ExecutionException;

@Setter @Getter
public class KakaoAPIException extends ExecutionException {

    String message;

    public KakaoAPIException(String message) {
        super(message);
    }
}
