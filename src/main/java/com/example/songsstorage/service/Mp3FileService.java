package com.example.songsstorage.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface Mp3FileService {

    List<String> listOfFiles();

    ByteArrayResource downloadFile(String fileName);

    boolean deleteFile(String fileName);

    void uploadFile(MultipartFile file) throws IOException;
}
