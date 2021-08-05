/**
 * Copyright (c) 2021 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package com.example.fileupload.controller;

import com.amazonaws.services.s3.model.ObjectMetadata;

import com.example.fileupload.service.FileService;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

@RestController
public class FileUploadController {
    public static final String FILE_HASH = "fileHash";

    @Autowired
    FileService fileService;

    public int chunkIndex = 0;

    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String handleUpload(HttpServletRequest request) {
        ObjectMetadata metadata = new ObjectMetadata();
        ServletFileUpload upload = new ServletFileUpload();
        String fileHash = request.getHeader(FILE_HASH);
        Date date = new Date();
        File inputFile = null;
        try {
            FileItemIterator iterStream =  upload.getItemIterator(request);
            String objectKey = "file"+ date.getTime();

            while (iterStream.hasNext()) {
                FileItemStream item = iterStream.next();
                try (InputStream inputStream = item.openStream()) {
                    if (!item.isFormField()) {
                        inputFile = new File(objectKey);
                        FileOutputStream outputStream = new FileOutputStream(inputFile);
                        validateSHA256Hash(inputStream, outputStream, fileHash);
                        fileService.uploadFile(inputFile, objectKey);
                    }
                }
            }
        } catch (Exception e) {
            if (inputFile != null && inputFile.delete())
                System.out.println("deleted file: " + inputFile.getName());
            return e.getMessage();
        }

        return "success";
    }

    private void validateSHA256Hash(InputStream inputStream, OutputStream outputStream, String fileHash) throws Exception {
        MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
        DigestInputStream digestInputStream = new DigestInputStream(inputStream, shaDigest);
        IOUtils.copy(digestInputStream, outputStream);
        digestInputStream.close();
        inputStream.close();
        outputStream.close();
        if (!Hex.encodeHexString(shaDigest.digest()).equalsIgnoreCase(fileHash)) {
            throw new Exception("Hash error: mismatch");
        }
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/upload-multichunks", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String handleUploadMultiChunks(HttpServletRequest request) {
        ServletFileUpload upload = new ServletFileUpload();
        String fileHash = request.getHeader(FILE_HASH);
        String objectKey = "file";
        File inputFile = null;
        int numberOfChunks = 0;
        System.out.println("----------------Receive request------------");
        try {
            FileItemIterator iterStream =  upload.getItemIterator(request);
            while (iterStream.hasNext()) {
                FileItemStream item = iterStream.next();
                try (InputStream inputStream = item.openStream()) {
                    if (!item.isFormField()) {
                        chunkIndex++;
                        System.out.println("Received " + chunkIndex + " chunks");
                        return  fileService.uploadFileViaStream(inputStream, objectKey, chunkIndex, numberOfChunks);
                    } else
                        if (item.getFieldName().equalsIgnoreCase("numberOfChunks")){
                            numberOfChunks = Integer.parseInt(IOUtils.toString(inputStream, StandardCharsets.UTF_8.name()));
                        }
                }
            }
        } catch (Exception e) {
            if (inputFile != null && inputFile.delete())
                System.out.println("deleted file: " + inputFile.getName());
            return e.getMessage();
        }

        return "success";
    }
}
