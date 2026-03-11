package com.congyu.word.generate;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Slf4j
public class ConfigPathUtil {

    public static String getConfigPath(){
        String userDir = System.getProperty("user.dir");

        String configPath;
        if (userDir.endsWith("staryea-ai-smart-patrol")){
            // ide中
            configPath =  String.join(File.separator,userDir,"patrol-server","src","main","resources");
        }else {
            configPath = String.join(File.separator,userDir);
        }
        log.info("configPath is {}",configPath);
        return configPath;
    }


    public static String getTempPath() throws IOException {
        String userDir = System.getProperty("user.dir");

        String tempPath;
        if (userDir.endsWith("staryea-ai-smart-patrol")){
            // ide中
            tempPath =  String.join(File.separator,userDir,"patrol-server","target","temp");
        }else {
            tempPath = String.join(File.separator,userDir,"temp");
        }
        Path path = Paths.get(tempPath);
        if(!Files.exists(path)){
            Files.createDirectory(path);
        }

        log.info("tempPath is {}",tempPath);
        return tempPath;
    }
}
