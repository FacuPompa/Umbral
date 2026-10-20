package com.umbral.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtConfigurationTest {
    @Test void rejectsMissingShortAndMalformedSigningKeys() {
        var config = new JwtConfiguration();
        assertThrows(IllegalStateException.class, () -> config.jwtKey(""));
        assertThrows(IllegalStateException.class, () -> config.jwtKey("not base64!"));
        assertThrows(IllegalStateException.class, () -> config.jwtKey("YWJj"));
    }
}
