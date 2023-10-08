package com.file.upload.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.file.upload.domain.FileMetadata;
import com.file.upload.dto.FileMetadataDto;
import com.file.upload.service.FileUploadService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {
	private final FileUploadService fileService;

	@PostMapping(path = "/upload")
	@ResponseStatus(code = HttpStatus.OK, reason = "OK")
	public FileMetadataDto upload(@RequestParam("file") MultipartFile file, @RequestParam("fileMetadata") String fileMetadata) {
		return fileService.handleUpload(file, fileMetadata);
	}

}
