package dust.service.micro.cache.redis;

import com.alibaba.fastjson.JSON;
import dust.service.micro.common.DustMsException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 对redis简单封装，用于String的缓存和读取
 * @author huangshengtao
 */
public class RedisInstance {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisInstance(StringRedisTemplate stringRedisTemplate) throws DustMsException {
        if (stringRedisTemplate == null) {
            throw new DustMsException("未能找到RedisTemplate");
        }
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public <T> T get(String key, Class<T> type) {
        if (StringUtils.isEmpty(key) || type == null) {
            return null;
        }

        String jsonString = stringRedisTemplate.opsForValue().get(key);
        return JSON.parseObject(jsonString, type);
    }


    public void set(String key, Object value) {
        if (value == null) {
            stringRedisTemplate.opsForValue().set(key, "");
        }

        if (value instanceof String) {
            stringRedisTemplate.opsForValue().set(key, (String)value);
        }

        if (value.getClass().isPrimitive()) {
            stringRedisTemplate.opsForValue().set(key, value.toString());
        }

        String json = JSON.toJSONString(value);
        stringRedisTemplate.opsForValue().set(key, json);
    }


}
