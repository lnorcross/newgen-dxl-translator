package com.newgen.spring.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class AppUtilities {

    public static Object dateConversion(String ndxlDate) {

        String[] dateParts = ndxlDate.split(",");

        String dateTimeString = dateParts[0];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:S");
        String formattedDateTime = dateTime.format(formatter2);
        ndxlDate= String.valueOf(formattedDateTime);

        return ndxlDate;
    }

    public static List<File> getXmlFiles(File directory) {
        List<File> xmlFiles = new ArrayList<>();
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".dxl")) {
                    xmlFiles.add(file);
                }
            }
        }
        return xmlFiles;
    }


    public static void Base64ToFile(String base64, String filePath) {
        String result = base64.replaceAll("\\r?\\n", "");


        String outputFilePath = filePath;

        try (FileOutputStream fos = new FileOutputStream(filePath); ) {
            String b64 = result.trim();
            byte[] decoder = Base64.getDecoder().decode(b64);

            fos.write(decoder);
            System.out.println("PDF File Saved");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
