package com.file.upload.dto;

import lombok.Data;

@Data
public class FileMetadataDto {
	private String id;
	private String fileName;
	private String accountUid;
	private String hash;
	private Double size;
}

