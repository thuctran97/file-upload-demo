package com.file.upload.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "account_settings")
public class AccountSettings {

	@Id
	private Long id;

	private Float quotaMax;
	private String quotaUnit;
}
