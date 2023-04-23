package com.example.songsstorage.service.impl;

import com.example.songsstorage.entity.FileEntity;
import com.example.songsstorage.repository.FileRepository;
import com.example.songsstorage.service.Mp3FileService;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class Mp3FileServiceImpl implements Mp3FileService {
    @Autowired
    Storage storage;

    @Autowired
    FileRepository fileRepository;

    @Value("${gcp.bucket.name}")
    private String bucketName;

    @Override
    public List<String> listOfFiles() {

        List<String> list = new ArrayList<>();
        List<FileEntity> all = fileRepository.findAll();
        Page<Blob> blobs = storage.list(bucketName);

        for (Blob blob : blobs.iterateAll()) {
            list.add(blob.getName());
        }
        return all.stream()
                .filter(fileEntity -> list.contains(fileEntity.getName()))
                .map(fileEntity -> fileEntity.getId().toString())
                .collect(Collectors.toList());
    }

    @Override
    public ByteArrayResource downloadFile(Long fileId) {
        Optional<FileEntity> fileEntityOptional = fileRepository.findById(fileId);
        String fileName = fileEntityOptional.map(FileEntity::getName).orElse(null);

        if (fileName != null) {
            Blob blob = storage.get(bucketName, fileName);
            return new ByteArrayResource(blob.getContent());
        }
        return null;
    }

    @Override
    public void deleteFile(Long fileId) {
        fileRepository.findById(fileId)
                .ifPresent(entity -> {
                    storage.get(bucketName, entity.getName()).delete();
                    fileRepository.delete(entity);
                });
    }

    @Override
    public FileEntity uploadFile(MultipartFile file) throws IOException {

        simpleValidation(file);

        BlobId blobId = BlobId.of(bucketName, file.getOriginalFilename());
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();
        FileEntity entity = createFileEntity(storage.create(blobInfo,file.getBytes()));

        return fileRepository.save(entity);
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
