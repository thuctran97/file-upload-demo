package com.file.upload.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class S3Service {
    public static String existingBucketName = "absolute-test-bucket";
    public TransferManager transferManager;
    public AmazonS3 s3Client;
    public InitiateMultipartUploadResult initResponse;
    public InitiateMultipartUploadRequest initRequest;
    public List<PartETag> partETags;
    public boolean isFirstPart;
    public MessageDigest shaDigest;
    public byte[] buf;

    public S3Service() throws NoSuchAlgorithmException {
        partETags = new ArrayList<PartETag>();
        buf = new byte[8192];
        shaDigest = MessageDigest.getInstance("SHA-256");
    }


    public void uploadFile(){

    }
}
