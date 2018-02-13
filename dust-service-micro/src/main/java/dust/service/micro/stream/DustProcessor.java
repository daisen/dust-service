package dust.service.micro.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * 系统的信息处理器
 * SpringCloudStream会自动根据{@link Input}和{@link Output}来创建相应的名称Bean
 * @author huangshengtao
 */
public interface DustProcessor {
    String INPUT_CHANNEL = "dustIn";
    String OUTPUT_CHANNEL = "dustOut";

    @Input(DustProcessor.INPUT_CHANNEL) //指定订阅通道为dustIn
    SubscribableChannel input();

    @Output(DustProcessor.OUTPUT_CHANNEL) //指定发布通道为dustOut
    MessageChannel output();
}
