package com.file.upload.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.file.upload.domain.FileMetadata;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

	@Query("SELECT f FROM FileMetadata f WHERE f.accountId = :accountId")
	List<FileMetadata> findAllByAccountId(@Param("accountId") String accountId);
}
