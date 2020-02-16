package cn.closeli.rtc.utils.signature;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    private static byte[] hex = { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102 };

    public static String encode(String rawString) {
        String md5String = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(rawString.getBytes());
            md5String = convertToHexString(md5.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if (null != md5String) {
            return md5String;
        }
        return md5String;
    }

    public static String convertToHexString(byte[] digests) {
        byte[] md5String = new byte[digests.length * 2];

        int index = 0;
        for (byte digest : digests) {
            md5String[index] = hex[(digest >> 4 & 0xF)];
            md5String[(index + 1)] = hex[(digest & 0xF)];
            index += 2;
        }

        return new String(md5String);
    }

}