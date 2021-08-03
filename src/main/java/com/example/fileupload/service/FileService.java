/**
 * Copyright (c) 2021 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package com.example.fileupload.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FileService {
    public static String existingBucketName = "absolute-test-bucket";
    public TransferManager transferManager;
    public AmazonS3 s3Client;

    public FileService() {
        s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.AP_SOUTHEAST_2)
                .withCredentials(new ProfileCredentialsProvider())
                .build();
        transferManager = TransferManagerBuilder.standard()
                .withS3Client(s3Client)
                .build();
    }

    private PutObjectRequest initUploadObject(String objectKey, File file) {
        PutObjectRequest request = new PutObjectRequest(existingBucketName, objectKey, file);
        request.setGeneralProgressListener(progressEvent -> System.out.println("Transferred bytes: " +
                progressEvent.getBytesTransferred()));
        return request;
    }

    public String uploadFile(File file, String objectKey) throws IOException {
        try{
            System.out.println("Uploading file: " + file.getName());
            PutObjectRequest request = initUploadObject(objectKey, file);
            Upload upload = transferManager.upload(request);
            upload.waitForCompletion();
            //transferManager.shutdownNow(true);
            System.out.println("completed upload: " + file.getName());
        }
        catch (AmazonClientException | InterruptedException  amazonClientException) {
            System.out.println("Unable to upload file, upload aborted.");
            amazonClientException.printStackTrace();
            return "upload error: " + amazonClientException.getMessage();
        }
        finally
        {
            try{
                if (file != null && file.delete())
                    System.out.println("deleted file: " + file.getName());
            }catch(Exception ex){}
        }
        return "upload success";
    }
}
