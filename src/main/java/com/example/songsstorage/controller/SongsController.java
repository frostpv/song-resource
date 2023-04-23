package com.example.songsstorage.controller;

import com.example.songsstorage.entity.FileEntity;
import com.example.songsstorage.service.impl.Mp3FileServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("resources")
public class SongsController {

    @Autowired
    Mp3FileServiceImpl fileService;

    /** This method return list of available files in storage
     * @return list of files
     */
    @GetMapping
    public ResponseEntity<List<String>> listOfFiles() {
        List<String> files = fileService.listOfFiles();
        return ResponseEntity.ok(files);
    }

    /** This method save file in storage
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping
    public ResponseEntity<FileEntity> uploadFile(@RequestParam MultipartFile file) throws IOException {
        try {
            FileEntity fileEntity = fileService.uploadFile(file);
            return ResponseEntity.ok(fileEntity);
        }
        catch (RuntimeException exc) {
            return ResponseEntity.badRequest()
                    .varyBy(exc.getMessage()).build();
        }

    }

    /** This method delete files from storage by list of Ids
     * @param ids
     * @return
     */
    @DeleteMapping
    public ResponseEntity<String> deleteFile(@RequestParam List<String> ids) {
        ids.forEach(id -> fileService.deleteFile(Long.valueOf(id)));
        return ResponseEntity.ok(" Files deleted successfully");
    }

    /** Use this method for download file from storage
     * @param fileId
     * @return
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId)  {
        Long aLong = Long.valueOf(fileId);
        ByteArrayResource resource = fileService.downloadFile(aLong);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + aLong + "\"");

        if (resource == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().
                contentType(MediaType.APPLICATION_OCTET_STREAM).
                headers(headers).body(resource);
    }
}
