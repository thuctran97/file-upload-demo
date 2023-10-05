package com.file.upload.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.file.upload.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {
	private final FileService fileService;

	@PostMapping(path = "/upload")
	public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file, @RequestParam("fileMetadata") String fileMetadata) {
		return new ResponseEntity<>(fileService.handleUpload(file, fileMetadata), HttpStatus.OK);
	}

}
