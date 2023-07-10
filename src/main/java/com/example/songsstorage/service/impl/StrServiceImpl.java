package com.example.songsstorage.service.impl;

import com.example.songsstorage.dto.StorageDto;
import com.example.songsstorage.service.StrService;
import com.example.songsstorage.util.StorageType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Service
public class StrServiceImpl implements StrService {
    @Value("${gcp.bucket.name}")
    private String bucketName;

    @Autowired
    @Lazy
    private RestTemplate restTemplate;

    private static final String BREAKER_NAME = "str";

    private final String tmpBucketName = "song-java-tmp";

    private static final String storageApp = "http://35.208.79.123:8099/str/storages";

    @Override
    @CircuitBreaker(name = BREAKER_NAME, fallbackMethod = "storageIfServiceISFail")
    public String getDefaultStorage() {
        StorageDto[] storageListDto = restTemplate.getForObject(storageApp, StorageDto[].class);

        assert storageListDto != null;
        StorageType storageType = Arrays.stream(storageListDto)
                .filter(Objects::nonNull)
                .findFirst()
                .map(StorageDto::getStorageType)
                .orElse(StorageType.PERMANENT);

        if(StorageType.PERMANENT == storageType) {
            return bucketName;
        }
        return tmpBucketName;
    }

    private String storageIfServiceISFail(Exception e) {
        return bucketName;
    }
}
