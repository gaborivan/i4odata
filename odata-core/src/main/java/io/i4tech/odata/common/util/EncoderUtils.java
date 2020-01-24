package io.i4tech.odata.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EncoderUtils {

    public static String encode(String value) {
        try {
            return URLEncoder.encode(value,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    public static String escape(String value) {
        return value.replace("'", "''");
    }
}
