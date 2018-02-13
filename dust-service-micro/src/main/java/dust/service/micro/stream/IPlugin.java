package dust.service.micro.stream;

import com.alibaba.fastjson.JSONObject;

/**
 * Stream分流的插件接口
 * 如果需要通过Stream处理的操作必须继承该接口
 * 默认只有一个方法run，用于执行操作。
 */
public interface IPlugin {
    void run(Object data);
}
