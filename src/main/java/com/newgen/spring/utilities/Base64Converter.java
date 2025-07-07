package com.newgen.spring.utilities;

import java.util.Base64;

public class Base64Converter {
    
    public static byte[] decodeBase64Content(String base64Content){
        return Base64.getDecoder().decode(base64Content.getBytes());
    }

    public static String encodeBase64Content(byte[] base64Content){
        return new String(Base64.getEncoder().encode(base64Content));
    }
}
