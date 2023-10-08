package com.file.upload.exception;

public class FileUploadException extends Exception{
	public FileUploadException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileUploadException(String message) {
		super(message);
	}
}
