package com.file.upload.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import com.file.upload.domain.FileMetadata;
import com.file.upload.dto.FileMetadataDto;

@Mapper
public interface FileMetadataMapper {
    FileMetadataMapper INSTANCE = Mappers.getMapper( FileMetadataMapper.class );

    FileMetadata toEntity(FileMetadataDto dto);
}
