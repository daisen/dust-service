/**
 * everything will be ok
 *
 * @author : huangxianhui
 * @Version :
 * @Date : 2016年2月3日 下午1:11:36
 */
package dust.service.core.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.SecureRandom;

/**
 * DES加密算法工具类
 */

@SuppressWarnings("restriction")
public class DESUtils {
    private static Key key;
    private static String KEY_STR = "myKey";// 密钥
    private static String CHARSETNAME = "UTF-8";// 编码
    private static String ALGORITHM = "DES";// 加密类型

    static {
        try {
            KeyGenerator generator = KeyGenerator.getInstance(ALGORITHM);
            generator.init(new SecureRandom(KEY_STR.getBytes()));
            key = generator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 对str进行DES加密
     * @param str
     * @return
     */
    public static String getEncryptString(String str) {
        BASE64Encoder base64encoder = new BASE64Encoder();
        try {
            byte[] bytes = str.getBytes(CHARSETNAME);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] doFinal = cipher.doFinal(bytes);
            return base64encoder.encode(doFinal);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 对str进行DES解密
     * @param str
     * @return
     */
    public static String getDecryptString(String str) {
        BASE64Decoder base64decoder = new BASE64Decoder();
        try {
            byte[] bytes = base64decoder.decodeBuffer(str);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] doFinal = cipher.doFinal(bytes);
            return new String(doFinal, CHARSETNAME);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 加密
    public static String getBase64(String str) {
        byte[] b = null;
        String s = null;
        try {
            b = str.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (b != null) {
            s = new BASE64Encoder().encode(b);
        }
        return s;
    }

    // 解密
    public static String getFromBase64(String s) {
        byte[] b;
        String result = null;
        if (s != null) {
            BASE64Decoder decoder = new BASE64Decoder();
            try {
                b = decoder.decodeBuffer(s);
                result = new String(b, "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static void main(String[] args) {

        String s = "MnhJSREk65GB0jBJZlDJ1HQqV+cm8ZAINVSE"
                + "DDs8ac4ACBVC5EQFmmqnP4gQpLu0jByEuRQCyTQ"
                + "puXWtbARTMTUEwF2IbbuebwwVDvPsWJdoZbGEEA"
                + "CIgwaBhJPJ";

        //
/*    	System.out.println(getFromBase64(getFromBase64(getFromBase64(getFromBase64(s)))));*/
        System.out.println(getBase64("TestHelloworld"));
        System.out.println(getFromBase64("VGVzdEhlbGxvd29ybGQ="));
/*    			String str = "Hello World";
                try{
    				byte[] encodeBase64 = Base64.encodeBase64(s.getBytes("UTF-8"));
    				System.out.println("RESULT: " + new String(encodeBase64));
    			} catch(UnsupportedEncodingException e){
    				e.printStackTrace();
    			}
    	*/


        String encode = getEncryptString(s);
        System.out.println(encode);
        String code = getDecryptString(encode);
        System.out.println(code);
    }


}
