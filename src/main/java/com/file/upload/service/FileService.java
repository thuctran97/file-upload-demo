package com.file.upload.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.file.upload.domain.FileMetadata;
import com.file.upload.dto.FileMetadataDto;
import com.file.upload.repository.FileMetadataRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

	private final ObjectMapper objectMapper;

	private final ModelMapper modelMapper;

	private final FileMetadataRepository fileMetadataRepository;

	public String handleUpload(MultipartFile file, String fileMetadata) throws Exception {
		try {
			FileMetadataDto dto  = objectMapper.readValue(fileMetadata, FileMetadataDto.class);
			File fileObj = convertMultiPartFileToFile(file);
			String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

			FileMetadata metadata = modelMapper.map(dto, FileMetadata.class);

			log.info("Metadata: {}", metadata);

			fileMetadataRepository.save(metadata);
		} catch (Exception e) {
			log.error("Exception: ", e);
			throw new Exception("Failed to upload");
		}
		return "Upload successfully";
	}



	private File convertMultiPartFileToFile(MultipartFile file) {
		File convertedFile = new File(file.getOriginalFilename());
		try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
			fos.write(file.getBytes());
		} catch (IOException e) {
			log.error("Error converting multipartFile to file", e);
		}
		return convertedFile;
	}
}
