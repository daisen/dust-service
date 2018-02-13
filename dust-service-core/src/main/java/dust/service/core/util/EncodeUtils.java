package dust.service.core.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncodeUtils {
    private static final String UTF8 = "UTF-8";
    public static final String HashKey = "Ok2*%@$,.Kfg{]<.36oP";

    // CMP加密
    public static String cmpEncrypt(String usercode, String password) {
        String primPassWord = md5(usercode.toLowerCase() + password);
        String hashKeyEncrypt = md5(HashKey);
        String hashCode = primPassWord.replace("-", "") + hashKeyEncrypt.replace("-", "");
        String secPassWord = md5(hashCode.toUpperCase()).replace("-", "");
        return secPassWord.toUpperCase();
    }

    public static String encryptmd5(String md5) {
        String hashKeyEncrypt = md5(HashKey);
        String hashCode = md5.replace("-", "") + hashKeyEncrypt.replace("-", "");
        String secPassWord = md5(hashCode.toUpperCase()).replace("-", "");
        return secPassWord.toUpperCase();
    }

    public static String md5(String str) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        MessageDigest md5 ;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(str.getBytes(UTF8));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        byte[] encodedValue = md5.digest();
        int j = encodedValue.length;
        char finalValue[] = new char[j * 2];
        int k = 0;
        for (byte encoded : encodedValue) {
            finalValue[k++] = hexDigits[encoded >> 4 & 0xf];
            finalValue[k++] = hexDigits[encoded & 0xf];
        }

        return new String(finalValue);
    }

}
