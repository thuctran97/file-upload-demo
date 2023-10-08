package com.file.upload.service;

import org.springframework.web.multipart.MultipartFile;

import com.file.upload.dto.FileMetadataDto;

public interface FileUploadService {

	FileMetadataDto handleUpload(MultipartFile multipartFile, String fileMetadata) throws Exception;
}
