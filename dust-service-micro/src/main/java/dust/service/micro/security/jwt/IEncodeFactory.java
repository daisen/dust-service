package dust.service.micro.security.jwt;

/**
 * 加密解密工厂接口，用于第三方实现
 * 该接口用于Text的加密解密，方便存放和处理
 * @author huangshengtao
 */
public interface IEncodeFactory {
    String encode(String type, String value);
    String decode(String type, String value);
}
