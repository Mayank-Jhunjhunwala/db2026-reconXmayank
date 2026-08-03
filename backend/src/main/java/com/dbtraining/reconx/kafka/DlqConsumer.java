package com.dbtraining.reconx.kafka;

import com.dbtraining.reconx.repository.DlqMessageRepository;
import com.dbtraining.reconx.repository.entity.DlqMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

@Component
public class DlqConsumer {
    private static final Logger log = LoggerFactory.getLogger(DlqConsumer.class);
    private final DlqMessageRepository dlqRepo;

    public DlqConsumer(DlqMessageRepository dlqRepo) {
        this.dlqRepo = dlqRepo;
    }

    @KafkaListener(topics = "trade-events-dlq", groupId = "dlq-service")
    public void onDlqMessage(ConsumerRecord<String, String> record, 
                             @Header(value = KafkaHeaders.EXCEPTION_MESSAGE, required = false) String exceptionMessage) {
        log.error("Received DLQ message on partition {} offset {}", record.partition(), record.offset());
        
        DlqMessage msg = new DlqMessage(
            "trade-events", 
            record.key() != null ? record.key() : "unknown", 
            record.value(),
            exceptionMessage != null ? exceptionMessage : "Unknown error"
        );
        dlqRepo.save(msg);
    }
}
