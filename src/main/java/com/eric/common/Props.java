package com.eric.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Props {
    public static Properties properties;

    static {
        properties = new Properties();
        try {
            properties.load(new FileInputStream("src/main/resources/application.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
