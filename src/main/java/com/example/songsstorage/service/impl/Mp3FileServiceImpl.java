package com.example.songsstorage.service.impl;

import com.example.songsstorage.entity.FileEntity;
import com.example.songsstorage.repository.FileRepository;
import com.example.songsstorage.service.Mp3FileService;
import com.example.songsstorage.service.RabbitService;
import com.example.songsstorage.service.StrService;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import model.Mp3FileResource;
import model.RabbitMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class Mp3FileServiceImpl implements Mp3FileService {
    @Autowired
    Storage storage;

    @Autowired
    FileRepository fileRepository;

    @Autowired
    RabbitService rabbitService;

    @Autowired
    StrService strService;

    @Override
    public List<String> listOfFiles() {

        List<String> list = new ArrayList<>();
        List<FileEntity> all = fileRepository.findAll();
        Page<Blob> blobs = storage.list(strService.getDefaultStorage());

        for (Blob blob : blobs.iterateAll()) {
            list.add(blob.getName());
        }
        return all.stream()
                .filter(fileEntity -> list.contains(fileEntity.getName()))
                .map(fileEntity -> fileEntity.getId().toString())
                .collect(Collectors.toList());
    }

    @Override
    public Mp3FileResource downloadFile(Long fileId) {
        Optional<FileEntity> fileEntityOptional = fileRepository.findById(fileId);
        String fileName = fileEntityOptional.map(FileEntity::getName).orElse(null);
        Mp3FileResource mp3FileResource = new Mp3FileResource();

        if (fileName != null) {
            Blob blob = storage.get(strService.getDefaultStorage(), fileName);

            mp3FileResource.setFilename(fileName);
            mp3FileResource.setFile(new ByteArrayResource(blob.getContent()));
            return mp3FileResource;
        }
        return null;
    }

    @Override
    public void deleteFile(Long fileId) {
        fileRepository.findById(fileId)
                .ifPresent(entity -> {
                    storage.get(strService.getDefaultStorage(), entity.getName()).delete();
                    fileRepository.delete(entity);
                });
    }

    @Override
    public FileEntity uploadFile(MultipartFile file) throws IOException {

        simpleValidation(file);

        BlobId blobId = BlobId.of(strService.getDefaultStorage(), file.getOriginalFilename());
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
        FileEntity entity = createFileEntity(storage.create(blobInfo,file.getBytes()));

        FileEntity fileEntity = fileRepository.save(entity);

        rabbitService.sendToQueue(new RabbitMessage(fileEntity.getId(), fileEntity.getName(), new Date()));

        return fileEntity;
    }

    private static void simpleValidation(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("file is empty");
        }

        String[] fileNameArray = file.getOriginalFilename().split("\\.");

        if (fileNameArray.length < 2 ) {
            throw new RuntimeException("file name is incorrect");
        }

        if (!Arrays.asList(fileNameArray).contains("mp3")) {
            throw new RuntimeException("validation is filed");
        }
    }

    private FileEntity createFileEntity(Blob blob) {
        FileEntity fileEntity =  new FileEntity();
        fileEntity.setName(blob.getName());
        fileEntity.setGeneratedId(blob.getGeneratedId());

        return fileEntity;
    }
}
