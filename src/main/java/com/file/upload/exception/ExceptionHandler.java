package com.file.upload.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class ExceptionHandler {
	@org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleException(Exception exception) {
		HttpStatus httpStatus = ErrorResponseType.getHttpStatusByException(exception.getClass());
		return new ResponseEntity<>(exception.getMessage(), httpStatus);
	}
}
