package com.umbral;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.umbral.support.PostgresTestConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class UmbralApplicationTests {

    @Test
    void contextLoads() {
    }

}
