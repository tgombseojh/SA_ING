package com.yellowbus.project.place.search.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString @AllArgsConstructor
public class ExceptionInfo {

    private String errorCode;
    private String errorMessage;

}
