package com.cacafly.exam.demo.middle.service;

import com.cacafly.exam.demo.front.controller.LoginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    @Value("${file.upload.dir}")
    Path rootLocation;

    @Inject
    private ResourceLoader resourceLoader;

    @Override
    public void store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
            }
            Files.copy(file.getInputStream(),
                    this.rootLocation.resolve(file.getOriginalFilename()),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    @Override
    public List<MultipartFile> storeFiles(MultipartFile... files) {
        List<MultipartFile> succeedFile = new ArrayList<>();
        if(files != null && files.length>0){
            int count = 0;
            for(MultipartFile file:files){
                try {
                    store(file);
                    succeedFile.add(file);
                    LOGGER.info("{} file uploaded.{}",++count,file.getOriginalFilename());
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(),e);
                }
            }
        }
        return succeedFile;
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(path -> this.rootLocation.relativize(path));
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        if(Files.exists(rootLocation,LinkOption.NOFOLLOW_LINKS)){
            FileSystemUtils.deleteRecursively(rootLocation.toFile());
        }
    }

    @Override
    public void init() {
        try {
            if(!Files.exists(rootLocation,LinkOption.NOFOLLOW_LINKS)){
                Files.createDirectory(rootLocation);
            }
            LOGGER.info("File location is been created.");
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
