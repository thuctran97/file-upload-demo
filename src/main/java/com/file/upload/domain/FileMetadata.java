package com.file.upload.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileMetadata {
    private String id;
    private String fileName;
    private String accountUid;
    private String hash;
    private String filePath;
    private Double size;
}
