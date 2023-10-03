package com.file.upload.dto;

import lombok.Data;

@Data
public class FileMetadataDto {
	private String fileName;
	private String accountId;
	private String hash;
	private Double size;
}

