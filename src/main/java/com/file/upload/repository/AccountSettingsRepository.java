package com.file.upload.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.file.upload.domain.AccountSettings;

public interface AccountSettingsRepository extends JpaRepository<AccountSettings, Long> {
}
