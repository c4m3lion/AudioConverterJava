package com.example.video_converter.services;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class AudioConversionService implements AudioConversionInterface{

    private static final Logger logger = LoggerFactory.getLogger(AudioConversionService.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Path root = Paths.get("audios");

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        try {
            Files.createDirectories(root);
            logger.info("Initializing folder for audio files");
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    @Override
    public String convertVideoToAudio(File source, String fileType) throws EncoderException {

        String targetFileName = source.getName().replace(".mp4", "." + fileType);
        File target = new File(this.root.resolve(targetFileName).toString());

        AudioAttributes audio = new AudioAttributes();
        audio.setCodec(fileType);
        audio.setBitRate(64000);
        audio.setChannels(2);
        audio.setSamplingRate(44100);

        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setOutputFormat(fileType);
        attrs.setAudioAttributes(audio);

        Encoder encoder = new Encoder();
        encoder.encode(new MultimediaObject(source), target, attrs);

        // Schedule the source and target files for deletion
        try {
            Files.deleteIfExists(source.toPath());
        } catch (Exception e) {
            logger.error("Could not delete source file: " + e.getMessage());
        }
        this.scheduleFileDeletion(target);

        return targetFileName;
    }

    @Override
    public void scheduleFileDeletion(File file) {
        scheduler.schedule(() -> {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (IOException e) {
                logger.error("Could not delete source file: " + e.getMessage());
            }
        }, 10, TimeUnit.MINUTES);
    }

    public String getRoot() {
        return root.toString();
    }
}
