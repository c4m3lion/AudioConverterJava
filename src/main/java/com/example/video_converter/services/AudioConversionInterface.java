package com.example.video_converter.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.EncoderException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface AudioConversionInterface {
    void init();

    void scheduleFileDeletion(File file);

//    void deleteFile(File file) throws IOException;

    String convertVideoToAudio(File source, String fileType) throws EncoderException;

}
