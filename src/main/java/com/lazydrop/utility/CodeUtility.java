package com.lazydrop.utility;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;


@Component
public class CodeUtility {
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();


    public String newAlphaNumericCode(int length) {
        if (length <= 0) throw new IllegalArgumentException("Code length must be positive");

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(CHARSET.length());
            sb.append(CHARSET.charAt(idx));
        }
        return sb.toString();
    }
}
