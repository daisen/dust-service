package dust.service.micro.stream;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import dust.service.core.util.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

/**
 * 推送信息记录类
 * 用于记录Stream收到的信息
 * @author huangshengtao
 */
@EnableBinding(DustProcessor.class)
@ConditionalOnProperty("dust.ms.stream.enable")
public class ReceiveBean {

    Logger logger = LoggerFactory.getLogger(ReceiveBean.class);

    /**
     * 订阅消息
     * 应用程序发布消息后，分组下的某一个应用程序的该方法就会收到消息
     * @param msg
     * @throws Exception
     */
    @StreamListener(DustProcessor.INPUT_CHANNEL)
    public void subscrib(String msg) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.info("subscrib:{}", msg);
        }

        if (StringUtils.isNotEmpty(msg)) {
            JSONObject json = null;
            try {
                json = JSON.parseObject(msg);
            } catch (Exception ex) {
                logger.error("接收到的信息不是json格式", ex);
            }

            if (json != null && json.containsKey("plugin")) {
                IPlugin plugin = (IPlugin) BeanUtils.getBean(json.getString("plugin"));
                if (plugin != null) {
                    plugin.run(json.get("data"));
                }
            }
        }
    }
}
