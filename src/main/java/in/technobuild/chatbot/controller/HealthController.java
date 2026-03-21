package in.technobuild.chatbot.controller;

import in.technobuild.chatbot.dto.response.HealthResponseDto;
import in.technobuild.chatbot.service.HealthCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HealthController {

    private final HealthCheckService healthCheckService;

    @GetMapping("/health")
    public ResponseEntity<HealthResponseDto> health() {
        log.info("Received /health request");
        HealthResponseDto response = healthCheckService.checkAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ready")
    public ResponseEntity<HealthResponseDto> ready() {
        log.info("Received /ready request");
        HealthResponseDto response = healthCheckService.checkAll();
        HttpStatus status = response.isAllUp() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(response);
    }
}
