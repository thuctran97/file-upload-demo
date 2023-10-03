package com.file.upload.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileMetadata {
	private String id;
	private String fileName;
	private String accountUid;
	private String hash;
	private String filePath;
	private Double size;
}
