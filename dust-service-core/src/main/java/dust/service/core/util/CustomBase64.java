/**
 * everything will be ok
 *
 * @author : huangxianhui
 * @Version :
 * @Date : 2015年11月18日 下午8:07:31
 */
package dust.service.core.util;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomBase64 {
    private static char[] base64EncodeChars = new char[]{'a', '2', 'C', '4', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v', // 40, 41, 42, 43, 44, 45, 46, 47,
            'Y', 'z', 'A', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', // 16,
            // 17,
            // 18,
            // 19,
            // 20,
            // 21,
            // 22,
            // 23,
            'w', 'y', 'x', 'Z', '0', '1', 'B', '3', 'D', '8', '6', '7', '5', '9', '+', '/'};

    private static byte[] base64DecodeChars = new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, // +
            // /
            52, 53, 1, 55, 3, 60, 58, 59, 57, 61, // 0-9
            -1, -1, -1, -1, -1, -1, -1, 26, 54, 2, 56, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 40, 41, 42, 43, 44, 45, 46, 47, 24, 51, // A-Z
            -1, -1, -1, -1, -1, -1, 0, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 16, 17, 18, 19, 20, 21, 22, 23, 48, 50, 49, 25,// a-z
            -1, -1, -1, -1, -1};

    /*
     * encode 对字符串进行编码
     */
    public static String encode(String str) {
        byte[] data = str.getBytes();
        StringBuilder sb = new StringBuilder();
        int len = data.length;
        int i = 0;
        int b1, b2, b3;

        while (i < len) {
            b1 = data[i++] & 0xff;
            // MOD 3 = 2添加两个“=”
            if (i == len) {
                sb.append(base64EncodeChars[b1 >>> 2]);
                sb.append(base64EncodeChars[(b1 & 0x3) << 4]);
                sb.append("==");
                break;
            }
            b2 = data[i++] & 0xff;
            // MOD 3 = 1添加一个“=”
            if (i == len) {
                sb.append(base64EncodeChars[b1 >>> 2]);
                sb.append(base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
                sb.append(base64EncodeChars[(b2 & 0x0f) << 2]);
                sb.append("=");
                break;
            }

            b3 = data[i++] & 0xff;
            // 将3个字节变为四个字节，从表中替换相应的字符
            sb.append(base64EncodeChars[b1 >>> 2]);
            sb.append(base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
            sb.append(base64EncodeChars[((b2 & 0x0f) << 2) | ((b3 & 0xc0) >>> 6)]);
            sb.append(base64EncodeChars[b3 & 0x3f]);
        }
        return sb.toString();
    }

    public static String decode(String str) {
        byte[] data = str.getBytes();
        for (int i = 0; i < data.length; i++) {
            if (data[i] == ' ') {
                data[i] = '+';
            }
        }
        int len = data.length;
        ByteArrayOutputStream buf = new ByteArrayOutputStream(len);
        int i = 0;
        int b1, b2, b3, b4;

        while (i < len) {

			/* b1 */
            do {
                b1 = base64DecodeChars[data[i++]];
            } while (i < len && b1 == -1);
            if (b1 == -1) {
                break;
            }

			/* b2 */
            do {
                b2 = base64DecodeChars[data[i++]];
            } while (i < len && b2 == -1);
            if (b2 == -1) {
                break;
            }
            buf.write((b1 << 2) | ((b2 & 0x30) >>> 4));

			/* b3 */
            do {
                b3 = data[i++];
                if (b3 == 61) {
                    return new String(buf.toByteArray());
                }
                b3 = base64DecodeChars[b3];
            } while (i < len && b3 == -1);
            if (b3 == -1) {
                break;
            }
            buf.write(((b2 & 0x0f) << 4) | ((b3 & 0x3c) >>> 2));

			/* b4 */
            do {
                b4 = data[i++];
                // 遇到“=”不需要处理
                if (b4 == 61) {
                    return new String(buf.toByteArray());
                }
                b4 = base64DecodeChars[b4];
            } while (i < len && b4 == -1);
            if (b4 == -1) {
                break;
            }
            buf.write(((b3 & 0x03) << 6) | b4);
        }
        /*
		 * if(Util.getCheck()==0){ return new String(buf.toByteArray()); } else{
		 * return null; }
		 */
        return new String(buf.toByteArray());
    }

    public static String deconf(String str) {
        return CustomBase64.decode(CustomBase64.decode(str));
    }

    public static void main(String[] args) {
        String testStr = "123456";
        System.out.println("加密前：" + testStr);
        String encodeStr = CustomBase64.encode(testStr);
        System.out.println("加密数据：" + encodeStr);
        encodeStr = CustomBase64.encode(encodeStr);
        System.out.println("加密数据：" + encodeStr);
        String decodeStr = CustomBase64.decode(encodeStr);
        System.out.println("解密数据：" + decodeStr);
        decodeStr = CustomBase64.decode(decodeStr);
        System.out.println("解密数据：" + decodeStr);
        System.out.println("解密数据：" + testStr.equals(decodeStr));

        Set<String> s = new HashSet<>();
        s.add("1");
        s.add("1");
        s.add("2");
        System.out.println(s.size());
        List<String> a = new ArrayList<>();
        a.addAll(s);
        for (String anA : a) {
            System.out.println(anA);
        }
    }
    // //加密前：fdp40
    // 加密数据：zmpwN4a=
    // 加密数据：em1wd0D0Yr0=
    // 解密数据：zmpwN4a=
    // 解密数据：fdp40
    // 解密数据：true

	/*
	 * 加密前：123456 加密数据：MrIZN4sB 加密数据：rvJJukD0c0I= 解密数据：MrIZN4sB 解密数据：123456
	 * 解密数据：true
	 */

}
