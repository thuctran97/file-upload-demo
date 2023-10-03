package com.file.upload.service;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

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
        s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.AP_SOUTHEAST_2)
                .withCredentials(new ProfileCredentialsProvider())
                .build();
        transferManager = TransferManagerBuilder.standard()
                .withS3Client(s3Client)
                .build();
        partETags = new ArrayList<PartETag>();
        buf = new byte[8192];
        shaDigest = MessageDigest.getInstance("SHA-256");
    }

    private void updateShaDigest(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        int n;
        while ((n = inputStream.read(buf)) > 0)
            shaDigest.update(buf, 0, n);
    }

    private void validateSha256Hash(String fileHash) throws Exception {
        byte hashes[] = shaDigest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashes) {
            sb.append(String.format("%02X", b));
        }
        String caculatedHash = sb.toString();
        System.out.println("Expected hash: " + fileHash +", actual hash: " + caculatedHash);
        if (!caculatedHash.equalsIgnoreCase(fileHash)){
            throw new Exception("Wrong hash");
        }
    }
    private void validateFileHash(String currentFilePath, String expectedHash) {
        int BUFFER_SIZE = 5*1024*1024;
        try (FileInputStream fis = new FileInputStream(currentFilePath);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            DigestInputStream dis = new DigestInputStream(bis, md);
            byte[] buffer = new byte[BUFFER_SIZE];
            int sizeRead = -1;
            while ((sizeRead = bis.read(buffer)) != -1) {
                md.update(buffer, 0, sizeRead);
            }
            dis.close();
            byte[] hash = md.digest();
            String calculatedHash = DatatypeConverter.printHexBinary(hash);
            if (!calculatedHash.equalsIgnoreCase(expectedHash)) {
                final String errorMsg = String.format("Wrong hash. Expected hash: %s, Actual hash: %s", expectedHash, calculatedHash);
            }
        } catch (NoSuchAlgorithmException | IOException exception) {
        }
    }

    public String uploadFileViaStream(InputStream inputStream, String objectKey, int chunkIndex, int numberOfChunks, String fileHash){
        File file = null;
        String fileName = objectKey + chunkIndex;
        long partSize = 5*1024*1024;
        try {
            file = new File(fileName);
            FileOutputStream out = new FileOutputStream(file);
            IOUtils.copy(inputStream, out);
            partSize = Math.min(partSize, file.length());
            if (chunkIndex == 1){
                System.out.println("Init upload request");
                initRequest = new InitiateMultipartUploadRequest(existingBucketName, objectKey);
                initResponse = s3Client.initiateMultipartUpload(initRequest);
                isFirstPart = false;
            }
            updateShaDigest(file);
            System.out.println("Uploading part: " + chunkIndex + ", size: " + partSize);
            UploadPartRequest uploadRequest = new UploadPartRequest()
                    .withBucketName(existingBucketName)
                    .withKey(objectKey)
                    .withUploadId(initResponse.getUploadId())
                    .withPartNumber(chunkIndex)
                    .withFileOffset(0)
                    .withFile(file)
                    .withPartSize(partSize);
            UploadPartResult uploadResult = s3Client.uploadPart(uploadRequest);
            partETags.add(uploadResult.getPartETag());
            file.delete();
            if (chunkIndex == numberOfChunks){
                validateSha256Hash(fileHash);
                CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(existingBucketName, objectKey,
                        initResponse.getUploadId(), partETags);
                s3Client.completeMultipartUpload(compRequest);
                System.out.println("Completed upload whole file");
            }
        } catch (Exception e){
            System.out.println("Unable to upload");
            e.printStackTrace();
            return "upload error" + e.getMessage();
        }
        return "success";
    }
}
