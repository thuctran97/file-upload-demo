package com.file.upload.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "storage_settings")
public class StorageSettings {

	@Id
	private Long id;
	private String accountId;
	private Float quotaMax;
}
