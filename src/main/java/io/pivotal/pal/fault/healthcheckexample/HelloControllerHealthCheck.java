package io.pivotal.pal.fault.healthcheckexample;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class HelloControllerHealthCheck implements HealthIndicator {
    private final HelloController helloController;

    public HelloControllerHealthCheck(HelloController helloController) {
        this.helloController = helloController;
    }

    @Override
    public Health health() {
        if (this.helloController.isFailedMessage())
            return Health.down()
                    .withDetail("Baaaadddd HelloController Request: ",
                            "id is not zero")
                    .build();
        else
            return Health.up().build();
    }
}
