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
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileService {
    public static String existingBucketName = "absolute-test-bucket";
    public TransferManager transferManager;
    public AmazonS3 s3Client;
    public Map uploadFileMap;
    public InitiateMultipartUploadResult initResponse;
    public InitiateMultipartUploadRequest initRequest;
    public long s3offset;
    public int fileIndex;
    public List<PartETag> partETags;
    public boolean isFirstPart;

    public FileService() {
        s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.AP_SOUTHEAST_2)
                .withCredentials(new ProfileCredentialsProvider())
                .build();
        transferManager = TransferManagerBuilder.standard()
                .withS3Client(s3Client)
                .build();
        uploadFileMap = new HashMap();
        s3offset = 0;
        fileIndex = 1;
        isFirstPart = true;
        partETags = new ArrayList<PartETag>();
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

    public String uploadFileViaStream(InputStream inputStream, String objectKey, Boolean isLastPart){
        File file = null;
        String fileName = objectKey + s3offset;
        long partSize = 5*1024*1024;
        try {
            System.out.println("uploading file: "+ fileName);
            file = new File(fileName);
            FileOutputStream out = new FileOutputStream(file);
            IOUtils.copy(inputStream, out);
            partSize = Math.min(partSize, file.length());
            if (isFirstPart){
                isFirstPart = false;
                System.out.println("init request");
                //uploadFileMap.put(objectKey,true);
                initRequest = new InitiateMultipartUploadRequest(existingBucketName, objectKey);
                initResponse = s3Client.initiateMultipartUpload(initRequest);
            }
            System.out.println("upload part: " + fileIndex + ", size: " + partSize + ", offset: " + s3offset);
            UploadPartRequest uploadRequest = new UploadPartRequest()
                    .withBucketName(existingBucketName)
                    .withKey(objectKey)
                    .withUploadId(initResponse.getUploadId())
                    .withPartNumber(fileIndex++)
                    .withFileOffset(0)
                    .withFile(file)
                    .withPartSize(partSize);
            System.out.println("Part number: " + uploadRequest.getPartNumber());
            s3offset += partSize;
            UploadPartResult uploadResult = s3Client.uploadPart(uploadRequest);
            partETags.add(uploadResult.getPartETag());
            file.delete();
            System.out.println("complete upload a part of :" + fileName);
            if (isLastPart){
                CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(existingBucketName, objectKey,
                        initResponse.getUploadId(), partETags);
                s3Client.completeMultipartUpload(compRequest);
                System.out.println("completed whole file");
                //uploadFileMap.remove(objectKey);
            }
        } catch (Exception e){
            System.out.println("Unable to upload");
            e.printStackTrace();
           // uploadFileMap.remove(objectKey);
            return "upload error" + e.getMessage();
        }
        return "success";
    }
}
