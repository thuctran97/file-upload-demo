package com.file.upload.controller;

import com.file.upload.dto.FileMetadataDto;
import com.file.upload.service.FileService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FileUploadController {
    public static final String FILE_HASH = "fileHash";

    private final FileService fileService;

    @PostMapping(path = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public FileMetadataDto upload(HttpServletRequest request) {
        return null;
    }


}
