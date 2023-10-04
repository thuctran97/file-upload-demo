package com.file.upload.controller;

import java.io.IOException;

import org.apache.commons.fileupload.FileUploadException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.file.upload.dto.FileMetadataDto;
import com.file.upload.service.FileService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FileUploadController {
	public static final String FILE_HASH = "fileHash";

	private final FileService fileService;

	@PostMapping(path = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public FileMetadataDto upload(@RequestParam("file") MultipartFile file, @RequestParam("fileMetadata") FileMetadataDto fileMetadataDto) throws IOException, FileUploadException {
		return fileService.handleUpload(file, fileMetadataDto);
	}

}
