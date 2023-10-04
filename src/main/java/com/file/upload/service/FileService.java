package com.file.upload.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.file.upload.dto.FileMetadataDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

	public static final String FILE_META_DATA_PARAM = "fileMetaData";

	private final ModelMapper modelMapper;


	public FileMetadataDto handleUpload(MultipartFile file, FileMetadataDto dto) {
		File fileObj = convertMultiPartFileToFile(file);
		String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
		log.info("File Name: {}", fileName);
		log.info("Dto: {}", dto);
		return null;
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
