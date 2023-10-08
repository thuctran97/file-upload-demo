package com.file.upload.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.file.upload.dto.FileMetadataDto;

import org.springframework.stereotype.Service;

import java.io.File;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 s3Client;

    private static final String BUCKET_PREFIX = "file-storage";

    public void uploadFile(File file, FileMetadataDto metadata) {
        String bucketName = String.format("%s-%s", BUCKET_PREFIX, metadata.getAccountId());
        s3Client.putObject(new PutObjectRequest(bucketName, metadata.getFileHash(), file));
    }
}
