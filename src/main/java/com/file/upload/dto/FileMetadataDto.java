package com.file.upload.dto;

import lombok.Data;

@Data
public class FileMetadataDto {
	private String accountId;
	private String fileName;
	private String fileHash;
	private Float size;
}

