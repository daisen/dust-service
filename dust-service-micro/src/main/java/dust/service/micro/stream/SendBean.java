package dust.service.micro.stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.support.MessageBuilder;

/**
 * 推送发送Bean
 * 用于向通道dust发送消息
 * @author huangshengtao
 */
@EnableBinding(DustProcessor.class)
@ConditionalOnProperty("dust.ms.stream.enable")
public class SendBean {

    @Autowired
    private DustProcessor dustProcessor;

    /**
     * 发送消息
     * 通过调用该方法，完成发布一个消息，此处只是发送字符串
     * 原则上可发送多种类型的数据，但是需要相应的MessageBuilder
     * 推荐使用字符串
     * @param str
     */
    public void sendMessage(String str) {
        this.dustProcessor.output().send(MessageBuilder.withPayload(str).build());
    }
}
