package com.sinwn.web.files.constant;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Defs {
//    public static final String temp_path = "";

    private static String filePath;

    static {
        Properties pps = new Properties();
        try {
            InputStream is = Defs.class.getClassLoader().getResourceAsStream("application.properties");
            pps.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //temp_path = pps.getProperty("temp_path");

        filePath = pps.getProperty("file_path");
    }

    public static String getFilePath() {
        return filePath;
    }
}
