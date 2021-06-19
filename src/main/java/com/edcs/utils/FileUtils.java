package com.edcs.utils;

import java.io.*;

public class FileUtils {

    public static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    public static String getConfigFile(){
        ClassLoader classLoader = FileUtils.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("config.json");
        String data = null;
        try {
            data = FileUtils.readFromInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
    public static String getConnectionsFile(){

        File initialFile = new File("connections.json");
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(initialFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String data = null;
        try {
            data = FileUtils.readFromInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }
}
