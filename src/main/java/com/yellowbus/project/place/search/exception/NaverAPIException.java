package com.yellowbus.project.place.search.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ExecutionException;

@Setter @Getter
public class NaverAPIException extends ExecutionException {

    String message;

    public NaverAPIException(String message) {
        super(message);
    }
}
