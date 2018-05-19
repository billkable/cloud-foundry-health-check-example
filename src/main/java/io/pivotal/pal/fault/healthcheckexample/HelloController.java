package io.pivotal.pal.fault.healthcheckexample;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    private boolean failedMessage;

    public HelloController() {
        this.failedMessage = false;
    }

    @GetMapping("/")
    public String getMessage() {
        return "Hello";
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getFailingMessage(@PathVariable long id) {
        if (id == 0)
            return ResponseEntity.ok().body(getMessage());
        else {
            this.failedMessage = true;
            return ResponseEntity.status(500).body("Baaaadddd Request");
        }
    }

    boolean isFailedMessage() {
        return failedMessage;
    }
}
