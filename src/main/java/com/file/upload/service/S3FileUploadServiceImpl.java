package com.file.upload.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.file.upload.domain.FileMetadata;
import com.file.upload.dto.FileMetadataDto;
import com.file.upload.exception.FileUploadException;
import com.file.upload.repository.FileMetadataRepository;
import com.file.upload.repository.StorageSettingsRepository;

import jakarta.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3FileUploadServiceImpl implements FileUploadService {

	private static final int CHECKSUM_VALIDATION_BUFFER_SIZE = 5 * 1024;
	private static final String BUCKET_PREFIX = "file-storage";

	private final ObjectMapper objectMapper;
	private final ModelMapper modelMapper;
	private final FileMetadataRepository fileMetadataRepository;
	private final StorageSettingsRepository storageSettingsRepository;
	private final AmazonS3 s3Client;

	@Override
	public FileMetadataDto handleUpload(MultipartFile multipartFile, String fileMetadata) throws Exception {
		File file = new File(getTempFileName(multipartFile.getOriginalFilename()));
		FileMetadata metadata;
		try {
			convertMultiPartFileToFile(multipartFile, file);
			metadata = objectMapper.readValue(fileMetadata, FileMetadata.class);
			validateFile(file, metadata);
			uploadFile(file, metadata);
			fileMetadataRepository.save(metadata);
		} catch (Exception e) {
			log.error("Exception while uploading:", e);
			throw e;
		} finally {
			file.delete();
		}
		return modelMapper.map(metadata, FileMetadataDto.class);
	}

	private void validateFile(File file, FileMetadata metadata) throws
		FileUploadException,
		IOException,
		NoSuchAlgorithmException {

		validateFileHash(file.getAbsolutePath(), metadata.getFileHash());
		metadata.setSize(getFileSizeInKB(file.getAbsolutePath()));

		List<FileMetadata> fileMetadataList = fileMetadataRepository.findAllByAccountId(metadata.getAccountId());
		validateIfExceedQuota(fileMetadataList, metadata);
		validateIfFileExists(fileMetadataList, metadata);
	}

	public void uploadFile(File file, FileMetadata metadata) {
		String bucketName = String.format("%s-%s", BUCKET_PREFIX, metadata.getAccountId());
		s3Client.putObject(new PutObjectRequest(bucketName, metadata.getFileHash(), file));
	}

	private void validateIfExceedQuota(List<FileMetadata> fileMetadataList, FileMetadata metadata) throws
		FileUploadException {
		float currentUsage = (float)fileMetadataList.stream().mapToDouble(FileMetadata::getSize).sum();
		float quota = storageSettingsRepository.findByAccountId(metadata.getAccountId()).getQuotaMax();
		log.info("currentUsage : {}, quota: {}", currentUsage, quota);
		float totalSize = currentUsage + metadata.getSize();
		if (totalSize > quota) {
			throw new FileUploadException("Exceed quota");
		}
	}

	private void validateIfFileExists(List<FileMetadata> fileMetadataList, FileMetadata metadata) throws
		FileUploadException {
		boolean isFileExist = fileMetadataList.stream()
			.anyMatch(fileMetadata -> fileMetadata.getFileHash().equals(metadata.getFileHash()));
		if (isFileExist) {
			throw new FileUploadException("File already exists");
		}
	}

	private void validateFileHash(String filePath, String expectedHash) throws
		FileUploadException,
		NoSuchAlgorithmException,
		IOException {
		try (FileInputStream fis = new FileInputStream(filePath);
			 BufferedInputStream bis = new BufferedInputStream(fis)) {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			DigestInputStream dis = new DigestInputStream(bis, md);
			byte[] buffer = new byte[CHECKSUM_VALIDATION_BUFFER_SIZE];
			int sizeRead = -1;
			while ((sizeRead = bis.read(buffer)) != -1) {
				md.update(buffer, 0, sizeRead);
			}
			dis.close();
			byte[] hash = md.digest();
			String calculatedHash = DatatypeConverter.printHexBinary(hash);
			if (!calculatedHash.equalsIgnoreCase(expectedHash)) {
				throw new FileUploadException("Wrong hash");
			}
		}
	}

	private String getTempFileName(String originalFileName) {
		return String.format("%s_%s", System.currentTimeMillis(), originalFileName);
	}

	private void convertMultiPartFileToFile(MultipartFile multipartFile, File file) {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(multipartFile.getBytes());
		} catch (IOException e) {
			log.error("Error converting multipartFile to file", e);
		}
	}

	private float getFileSizeInKB(String filePath) throws IOException {
		long fileSize = Files.size(Paths.get(filePath));
		return (float)fileSize / 1024;
	}
}
