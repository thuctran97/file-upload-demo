package com.file.upload.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.file.upload.domain.FileMetadata;
import com.file.upload.dto.FileMetadataDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

	public static final String HEADER_ACCOUNT_UID = "accountUid";
	public static final String FILE_META_DATA_PARAM = "fileMetaData";
	public static final String FILE_UPLOAD_PARAM = "file";

	private final ModelMapper modelMapper;


	public FileMetadataDto handleUpload(HttpServletRequest request) throws IOException, FileUploadException {
		ServletFileUpload upload = new ServletFileUpload();
		ObjectMapper mapper = new ObjectMapper();

		File inputFile = null;
		String fileName = null;
		FileMetadataDto dto = null;
		FileItemIterator iterStream = upload.getItemIterator(request);
		while (iterStream.hasNext()) {
			FileItemStream item = iterStream.next();
			if (item.isFormField() && FILE_META_DATA_PARAM.equals(item.getFieldName())) {
				dto = mapper.readValue(item.openStream(), FileMetadataDto.class);
				log.info("DTO: {}", dto);
				continue;
			}
			if (!item.isFormField()) {
				log.info("FILE");
				fileName = item.getName().replaceAll("[\n\r\t]", "");
				inputFile = FileUtils.getFile(String.format("%s%s%s%s%s", System.getProperty("java.io.tmpdir"),
					File.separator, LocalTime.now().toNanoOfDay(), "_", fileName));
				FileUtils.copyInputStreamToFile(item.openStream(), inputFile);
			}
		}

		if (null != dto && null != inputFile) {
			dto.setFileName(fileName);
			FileMetadata metadata = buildFileMetaData(dto);
			return null;
		}
		return null;
	}

	private FileMetadata buildFileMetaData(FileMetadataDto dto) {
		return modelMapper.map(dto, FileMetadata.class);
	}
}
