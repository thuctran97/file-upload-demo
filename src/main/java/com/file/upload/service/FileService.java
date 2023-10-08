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
public class FileService {

	public static final int CHECKSUM_VALIDATION_BUFFER_SIZE = 5 * 1024;
	private final ObjectMapper objectMapper;
	private final ModelMapper modelMapper;
	private final S3Service s3Service;
	private final FileMetadataRepository fileMetadataRepository;
	private final StorageSettingsRepository storageSettingsRepository;

	public String handleUpload(MultipartFile multipartFile, String fileMetadata) {
		File file = new File(getTempFileName(multipartFile.getOriginalFilename()));
		try {
			FileMetadataDto dto = objectMapper.readValue(fileMetadata, FileMetadataDto.class);
			convertMultiPartFileToFile(multipartFile, file);
			log.info("Dto: {}", dto);
			validateFile(file, dto);
			s3Service.uploadFile(file, dto);
			fileMetadataRepository.save(modelMapper.map(dto, FileMetadata.class));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			file.delete();
		}
		return "Upload successfully";
	}

	private void validateFile(File file, FileMetadataDto dto) throws
		FileUploadException,
		IOException,
		NoSuchAlgorithmException {

		validateFileHash(file.getAbsolutePath(), dto.getFileHash());
		dto.setSize(getFileSizeInKB(file.getAbsolutePath()));

		List<FileMetadata> fileMetadataList = fileMetadataRepository.findAllByAccountId(dto.getAccountId());
		validateIfExceedQuota(fileMetadataList, dto);
		validateIfFileExists(fileMetadataList, dto);
	}

	private void validateIfExceedQuota(List<FileMetadata> fileMetadataList, FileMetadataDto dto) throws
		FileUploadException {
		float currentUsage = (float)fileMetadataList.stream().mapToDouble(FileMetadata::getSize).sum();
		float quota = storageSettingsRepository.findByAccountId(dto.getAccountId()).getQuotaMax();
		log.info("currentUsage : {}, quota: {}", currentUsage, quota);
		float totalSize = currentUsage + dto.getSize();
		if (totalSize > quota) {

			throw new FileUploadException("Exceed quota");
		}
	}

	private void validateIfFileExists(List<FileMetadata> fileMetadataList, FileMetadataDto dto) throws
		FileUploadException {
		boolean isFileExist = fileMetadataList.stream()
			.anyMatch(metadata -> metadata.getFileHash().equals(dto.getFileHash()));
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
