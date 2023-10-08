package com.file.upload.exception;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorResponseType {
	BAD_REQUEST(List.of(
		FileUploadException.class
	), HttpStatus.BAD_REQUEST),
	INTERNAL_SERVER_ERROR(List.of(
		NoSuchAlgorithmException.class,
		IOException.class
	), HttpStatus.INTERNAL_SERVER_ERROR);

	private static final Map<Class<? extends Exception>, HttpStatus> EXCEPTION_MAP = Collections.unmodifiableMap(
		initialize());
	private final List<Class<? extends Exception>> classes;
	private final HttpStatus httpStatus;

	ErrorResponseType(List<Class<? extends Exception>> classes, HttpStatus httpStatus) {
		this.classes = classes;
		this.httpStatus = httpStatus;
	}

	private static Map<Class<? extends Exception>, HttpStatus> initialize() {
		Map<Class<? extends Exception>, HttpStatus> map = new HashMap<>();
		for (ErrorResponseType value : values()) {
			for (Class<? extends Exception> clazz : value.getClasses()) {
				map.put(clazz, value.getHttpStatus());
			}
		}
		return map;
	}

	public static HttpStatus getHttpStatusByException(Class<? extends Exception> exceptionClass) {
		HttpStatus value = EXCEPTION_MAP.get(exceptionClass);
		if (value == null) {
			value = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return value;
	}
}
