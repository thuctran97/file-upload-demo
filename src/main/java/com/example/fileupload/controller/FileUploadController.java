/**
 * Copyright (c) 2021 Absolute Software Corporation. All rights reserved. Reproduction or
 * transmission in whole or in part, in any form or by any means, electronic, mechanical or
 * otherwise, is prohibited without the prior written consent of the copyright owner.
 */
package com.example.fileupload.controller;


import com.example.fileupload.service.FileService;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.http.HttpServletRequest;

@RestController
public class FileUploadController {
    public static final String FILE_HASH = "fileHash";

    @Autowired
    FileService fileService;

    public int chunkIndex = 0;

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/upload-demo", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String demoUploadMultiChunks(HttpServletRequest request) {
        try {
            FileOutputStream fos = new FileOutputStream("temp/result",true);
            for (int index = 0; index < 10; index++){
                Path path = Path.of("temp/" + index);
                System.out.println("Exists: "+Files.exists(path));
                File file = new File(String.valueOf(path));
                FileInputStream fis = new FileInputStream(file);
                byte[] fileBytes = new byte[(int) file.length()];
                fos.write(fileBytes);
                fos.flush();
                fis.close();
            }
            fos.close();
        }catch (Exception exception){
            exception.printStackTrace();
        }
        System.out.println("The end");
        return "true";
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/upload-multichunks", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String handleUploadMultiChunks(HttpServletRequest request) {
        ServletFileUpload upload = new ServletFileUpload();
        String fileHash = null;
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
                        return  fileService.uploadFileViaStream(inputStream, objectKey, chunkIndex, numberOfChunks, fileHash);
                    } else {
                        if (item.getFieldName().equalsIgnoreCase("numberOfChunks")){
                            numberOfChunks = Integer.parseInt(IOUtils.toString(inputStream, StandardCharsets.UTF_8.name()));
                        }
                        if (item.getFieldName().equalsIgnoreCase("fileHash")){
                            fileHash = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
                        }
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
