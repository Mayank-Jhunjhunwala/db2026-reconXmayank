package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.repository.DlqMessageRepository;
import com.dbtraining.reconx.repository.entity.DlqMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/admin/dlq")
@PreAuthorize("hasRole('ADMIN')")
public class DlqAdminController {

    private final DlqMessageRepository dlqRepo;
    private final KafkaTemplate<String, String> producer;

    public DlqAdminController(DlqMessageRepository dlqRepo, KafkaTemplate<String, String> producer) {
        this.dlqRepo = dlqRepo;
        this.producer = producer;
    }

    @PostMapping("/replay")
    public ResponseEntity<?> replay(@RequestParam("eventId") String eventId) {
        Optional<DlqMessage> opt = dlqRepo.findByEventId(eventId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        
        DlqMessage msg = opt.get();
        producer.send(msg.getTopic(), msg.getEventId(), msg.getPayload());
        dlqRepo.delete(msg);

        return ResponseEntity.ok(Map.of(
                "replayed", true,
                "eventId", eventId,
                "topic", msg.getTopic()
        ));
    }
}
