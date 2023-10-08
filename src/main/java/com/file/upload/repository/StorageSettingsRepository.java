package com.file.upload.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.file.upload.domain.StorageSettings;

public interface StorageSettingsRepository extends JpaRepository<StorageSettings, Long> {

	@Query("SELECT e FROM StorageSettings e WHERE e.accountId = :accountId")
	StorageSettings findByAccountId(@Param("accountId") String accountId);
}
