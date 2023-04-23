package com.example.songsstorage.service;

import com.example.songsstorage.entity.FileEntity;
import model.Mp3FileResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface Mp3FileService {

    List<String> listOfFiles();

    Mp3FileResource downloadFile(Long fileId);

    void deleteFile(Long fileId);

    FileEntity uploadFile(MultipartFile file) throws IOException;
}
