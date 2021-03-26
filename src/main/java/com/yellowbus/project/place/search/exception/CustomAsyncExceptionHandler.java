package com.yellowbus.project.place.search.exception;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Method;

// return type void async() error handle
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {

        System.out.println("Exception message - " + throwable.getMessage());
        System.out.println("Method name - " + method.getName());
        for (Object param : obj) {
            System.out.println("Parameter value - " + param);
        }

        throw new ApiException(" CustomAsyncExceptionHandler ", HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
