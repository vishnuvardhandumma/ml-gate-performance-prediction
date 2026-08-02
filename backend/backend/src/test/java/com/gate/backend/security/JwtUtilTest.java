package com.gate.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class JwtUtilTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(JwtUtil.class);

    @Test
    void shouldCreateJwtUtilWithDefaultValuesWhenPropertiesAreMissing() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(JwtUtil.class);
            JwtUtil jwtUtil = context.getBean(JwtUtil.class);
            assertThat(jwtUtil.generateToken("user@example.com")).isNotBlank();
        });
    }
}
