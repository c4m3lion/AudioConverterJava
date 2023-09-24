package com.example.video_converter.controller;

import com.example.video_converter.services.AudioConversionService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
public class AudioConversionController {

    private final AudioConversionService audioConversionService;
    private static final String AUDIO_FILES_DIR = "src/main/resources/static/audio";

    public AudioConversionController(AudioConversionService audioConversionService) {
        this.audioConversionService = audioConversionService;
    }

    @PostMapping("/convert")
    public ResponseEntity<String> convertVideoToAudio(@RequestParam("video") MultipartFile video,
                                                      @RequestParam("fileType") String fileType) throws Exception {
        File source = convertMultipartFileToFile(video);

        var targetFileName = audioConversionService.convertVideoToAudio(source, fileType);


        // Create the URL for the audio file
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/download/")
                .path(targetFileName)
                .toUriString();

        return ResponseEntity.ok().body(fileDownloadUri);
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        Path filePath = Paths.get(audioConversionService.getRoot(), filename);
        Resource resource = new FileSystemResource(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
}
