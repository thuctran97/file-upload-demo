package com.file.upload.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "file_metadata")
public class FileMetadata {
	@Id
	private Long id;

	private String fileName;
	private String accountUid;
	private String hash;
	private String filePath;
	private Double size;
}
