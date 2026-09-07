package ir.aspireapps.linker.analysisservice.kafka;

import ir.aspireapps.linker.common.utility.KafkaTopicsConstants;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.stereotype.Component;

@Component
public class KafkaErrorHandler {
    @Bean
    public DefaultErrorHandler defaultErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        var recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) ->
                        new TopicPartition(KafkaTopicsConstants.ANALYTICS_ERROR_TOPIC, record.partition())
        );
        return new DefaultErrorHandler(recoverer);
    }
}
