package com.file.upload.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileUploadException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.file.upload.dto.FileMetadataDto;
import com.file.upload.service.FileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FileUploadController {
	public static final String FILE_HASH = "fileHash";

	private final FileService fileService;

	@PostMapping(path = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public FileMetadataDto upload(HttpServletRequest request) throws IOException, FileUploadException {
		return fileService.handleUpload(request);
	}

}
